package br.com.pedromagno.core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SimTimeTest {

    @Test
    fun `SimTime addition with double`() {
        val t = SimTime(2.0) + 3.0
        assertEquals(5.0, t.value)
    }

    @Test
    fun `SimTime addition with SimTime`() {
        val t = SimTime(2.0) + SimTime(3.0)
        assertEquals(5.0, t.value)
    }

    @Test
    fun `SimTime comparison`() {
        assertTrue(SimTime(1.0) < SimTime(2.0))
        assertTrue(SimTime(2.0) > SimTime(1.0))
        assertEquals(0, SimTime(1.0).compareTo(SimTime(1.0)))
    }

    @Test
    fun `negative SimTime throws`() {
        assertThrows<IllegalArgumentException> { SimTime(-1.0) }
    }

    @Test
    fun `negative delay addition throws`() {
        assertThrows<IllegalArgumentException> { SimTime(1.0) + (-1.0) }
    }

    @Test
    fun `ZERO constant is zero`() {
        assertEquals(0.0, SimTime.ZERO.value)
    }
}
