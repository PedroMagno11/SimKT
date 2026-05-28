package br.com.pedromagno

import br.com.pedromagno.core.Simulation
import br.com.pedromagno.exemplos.CentralComando
import br.com.pedromagno.exemplos.Radar
import br.com.pedromagno.exemplos.Sensor


fun main() {
    val simulation = Simulation()

    simulation.add { env, bus -> Sensor(env, bus) }
    simulation.add { env, bus -> Radar(env, bus) }
    simulation.add { env, bus -> CentralComando(env, bus) }

    simulation.run()

}
