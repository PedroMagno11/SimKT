package br.com.pedromagno.examples

import br.com.pedromagno.communication.channel.SimChannel
import br.com.pedromagno.core.Environment
import br.com.pedromagno.core.SimTime
import br.com.pedromagno.process.ISimProcess
import br.com.pedromagno.process.SimProcessContext
import kotlin.math.sqrt
import kotlin.random.Random

data class Vec2(
    var x: Double,
    var y: Double
)

data class DroneState(
    val position: Vec2,
    val velocity: Vec2
)

data class RadarMeasurement(
    val time: Double,
    val measuredX: Double,
    val measuredY: Double
)

class Drone(
    private val state: DroneState,
    private val dt: Double
) : ISimProcess {

    override suspend fun run(ctx: SimProcessContext) {
        while (true) {
            state.position.x += state.velocity.x * dt
            state.position.y += state.velocity.y * dt

            ctx.env.monitor.record(
                ctx.now,
                "DRONE real = (${state.position.x.format()}, ${state.position.y.format()})"
            )

            ctx.hold(dt)
        }
    }
}

class Radar(
    private val drone: DroneState,
    private val output: SimChannel<RadarMeasurement>,
    private val dt: Double,
    private val noiseStd: Double
) : ISimProcess {

    private val random = Random(42)

    override suspend fun run(ctx: SimProcessContext) {
        while (true) {
            val measurement = RadarMeasurement(
                time = ctx.now.value,
                measuredX = drone.position.x + random.nextGaussian() * noiseStd,
                measuredY = drone.position.y + random.nextGaussian() * noiseStd
            )

            ctx.send(output, measurement)

            ctx.env.monitor.record(
                ctx.now,
                "RADAR mediu = (${measurement.measuredX.format()}, ${measurement.measuredY.format()})"
            )

            ctx.hold(dt)
        }
    }
}

class Kalman2D(
    initialX: Double,
    initialY: Double,
    private val dt: Double,
    private val processNoise: Double,
    private val measurementNoise: Double
) {
    private var x = doubleArrayOf(initialX, initialY, 0.0, 0.0)

    private var p = Array(4) { i ->
        DoubleArray(4) { j -> if (i == j) 1000.0 else 0.0 }
    }

    fun predict() {
        val f = arrayOf(
            doubleArrayOf(1.0, 0.0, dt, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0, dt),
            doubleArrayOf(0.0, 0.0, 1.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 1.0)
        )

        val q = Array(4) { i ->
            DoubleArray(4) { j -> if (i == j) processNoise else 0.0 }
        }

        x = multiply(f, x)
        p = add(multiply(multiply(f, p), transpose(f)), q)
    }

    fun update(zx: Double, zy: Double) {
        val h = arrayOf(
            doubleArrayOf(1.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0, 0.0)
        )

        val r = arrayOf(
            doubleArrayOf(measurementNoise, 0.0),
            doubleArrayOf(0.0, measurementNoise)
        )

        val z = doubleArrayOf(zx, zy)
        val y = subtract(z, multiply(h, x))

        val s = add(multiply(multiply(h, p), transpose(h)), r)
        val k = multiply(multiply(p, transpose(h)), inverse2x2(s))

        x = add(x, multiply(k, y))

        val i = identity(4)
        p = multiply(subtract(i, multiply(k, h)), p)
    }

    fun estimatedPosition(): Vec2 {
        return Vec2(x[0], x[1])
    }
}

class KalmanEstimator(
    private val input: SimChannel<RadarMeasurement>,
    private val filter: Kalman2D
) : ISimProcess {

    override suspend fun run(ctx: SimProcessContext) {
        while (true) {
            val measurement = ctx.receive(input)

            filter.predict()
            filter.update(measurement.measuredX, measurement.measuredY)

            val estimated = filter.estimatedPosition()

            ctx.env.monitor.record(
                ctx.now,
                "KALMAN estimou = (${estimated.x.format()}, ${estimated.y.format()})"
            )
        }
    }
}

private fun Double.format(): String = "%.2f".format(this)

private fun Random.nextGaussian(): Double {
    val u1 = nextDouble()
    val u2 = nextDouble()
    return sqrt(-2.0 * kotlin.math.ln(u1)) * kotlin.math.cos(2.0 * Math.PI * u2)
}

private fun identity(n: Int): Array<DoubleArray> =
    Array(n) { i -> DoubleArray(n) { j -> if (i == j) 1.0 else 0.0 } }

private fun transpose(a: Array<DoubleArray>): Array<DoubleArray> =
    Array(a[0].size) { i -> DoubleArray(a.size) { j -> a[j][i] } }

private fun add(a: DoubleArray, b: DoubleArray): DoubleArray =
    DoubleArray(a.size) { i -> a[i] + b[i] }

private fun subtract(a: DoubleArray, b: DoubleArray): DoubleArray =
    DoubleArray(a.size) { i -> a[i] - b[i] }

private fun add(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> =
    Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] + b[i][j] } }

private fun subtract(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> =
    Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] - b[i][j] } }

private fun multiply(a: Array<DoubleArray>, x: DoubleArray): DoubleArray =
    DoubleArray(a.size) { i -> a[i].indices.sumOf { j -> a[i][j] * x[j] } }

private fun multiply(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> =
    Array(a.size) { i ->
        DoubleArray(b[0].size) { j ->
            b.indices.sumOf { k -> a[i][k] * b[k][j] }
        }
    }

private fun inverse2x2(m: Array<DoubleArray>): Array<DoubleArray> {
    val det = m[0][0] * m[1][1] - m[0][1] * m[1][0]

    require(kotlin.math.abs(det) > 1e-9) {
        "Matriz singular no Kalman."
    }

    return arrayOf(
        doubleArrayOf(m[1][1] / det, -m[0][1] / det),
        doubleArrayOf(-m[1][0] / det, m[0][0] / det)
    )
}

fun main() {
    val env = Environment()

    val radarMeasurements = env.channel<RadarMeasurement>(
        name = "radar-measurements",
        capacity = 100
    )

    val droneState = DroneState(
        position = Vec2(0.0, 0.0),
        velocity = Vec2(15.0, 8.0)
    )

    val dt = 1.0

    env.process(
        Drone(
            state = droneState,
            dt = dt
        )
    )

    env.process(
        Radar(
            drone = droneState,
            output = radarMeasurements,
            dt = dt,
            noiseStd = 20.0
        )
    )

    env.process(
        KalmanEstimator(
            input = radarMeasurements,
            filter = Kalman2D(
                initialX = 0.0,
                initialY = 0.0,
                dt = dt,
                processNoise = 0.1,
                measurementNoise = 400.0
            )
        )
    )

    env.run(until = SimTime(30.0))

    env.monitor.printAll()
}