# SimKT

SimKT é uma biblioteca experimental de simulação escrita em Kotlin. O projeto nasceu com o objetivo de estudar e construir, passo a passo, um pequeno motor de simulação baseado em eventos, entidades e comunicação por barramento de eventos.

A ideia central é permitir a criação de cenários simulados onde entidades independentes interagem entre si por meio de eventos no ambiente.

---

## Objetivo do projeto

O SimKT busca oferecer uma base simples para modelar simulações orientadas a eventos em Kotlin.

Nesta versão inicial, a biblioteca já permite:

- criar um ambiente de simulação;
- agendar eventos em uma fila de prioridade;
- criar entidades simuladas;
- iniciar múltiplas entidades dentro de uma simulação;
- comunicar entidades usando um `EventBus`;

---

## Estrutura atual do projeto

```text
Simkt/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── kotlin/
            ├── Main.kt
            ├── core/
            │   ├── Environment.kt
            │   ├── EventBus.kt
            │   ├── EventoPeriodico.kt
            │   ├── SimEntity.kt
            │   ├── SimEvent.kt
            │   └── Simulation.kt
            └── exemplos/
                ├── Drone.kt
                ├── Radar.kt
                └── CentralDeVigilancia.kt
```

---

## Núcleo da biblioteca

### Environment

O `Environment` representa o ambiente da simulação.

Ele é responsável por manter a fila de eventos e executar as ações agendadas.

```kotlin
val env = Environment()

env.schedule {
    println("Evento executado")
}

env.run()
```

Atualmente, o `Environment` possui:

- `now`: tempo atual da simulação;
- `schedule`: agenda uma ação para execução;
- `run`: executa os eventos pendentes;
- `hasEvent`: verifica se ainda existem eventos na fila.

Nesta fase, a ordenação dos eventos é feita principalmente por prioridade e por identificador interno do evento.

---

### SimEvent

O `SimEvent` representa uma ação agendada dentro do ambiente.

```kotlin
 data class SimEvent(
    val id: Long,
    val priority: Int,
    val action: Environment.() -> Unit
)
```

Cada evento possui:

- `id`: identificador interno usado para manter a ordem de criação;
- `priority`: prioridade de execução;
- `action`: função que será executada pelo ambiente.

---

### EventBus

O `EventBus` permite comunicação entre entidades usando nomes de eventos.

```kotlin
bus.on("drone_detectado") { payload ->
    val drone = payload as String
    println("Drone recebido: $drone")
}

bus.emit("drone_detectado", "Drone A")
```

Ele possui dois métodos principais:

- `on(eventName, action)`: registra um listener para um evento;
- `emit(eventName, payload)`: publica um evento para todos os listeners registrados.

---

### SimEntity

`SimEntity` é a classe base para qualquer entidade simulada.

```kotlin
abstract class SimEntity(
    protected val env: Environment,
    protected val bus: EventBus
) {
    abstract fun start()
}
```

Cada entidade recebe:

- o ambiente de simulação;
- o barramento de eventos;
- um método `start`, onde seus eventos e listeners são configurados.

---

### Simulation

A classe `Simulation` funciona como uma fachada para criar e executar uma simulação com várias entidades.

```kotlin
val simulation = Simulation()

simulation.add { env, bus -> Drone(env, bus) }
simulation.add { env, bus -> Radar(env, bus) }
simulation.add { env, bus -> CentralDeVigilancia(env, bus) }

simulation.run()
```

Ela encapsula:

- um `Environment`;
- um `EventBus`;
- uma lista de entidades;
- o processo de inicialização e execução.

---

## Exemplo simples: Drone, Radar e Central de Vigilância

O projeto possui um exemplo onde:

1. `Drone` se move.
2. O Radar detecta o drone e publica o evento `drone_detectado`.
3. `Central de Vigilância` escuta esse evento e inicia o rastreamento.
4. O radar publica o evento `drone_rastreado`.
5. `Central de Vigilância` recebe a informação do drone rastreado.

```kotlin
fun main() {
    val simulation = Simulation()

    simulation.add { env, bus -> Drone(env, bus) }
    simulation.add { env, bus -> Radar(env, bus) }
    simulation.add { env, bus -> CentralComando(env, bus) }

    simulation.run()
}
```


Esse exemplo é útil para estudar cenários de detecção, sensores distribuídos e eventos publicados no ambiente.

---

## Como executar

Clone o projeto:

```bash
git clone https://github.com/PedroMagno11/SimKT
cd Simkt
```

Compile com Maven:

```bash
mvn clean package
```

Execute a aplicação principal:

```bash
mvn exec:java
```

Caso o plugin de execução não encontre a classe principal, ajuste o `mainClass` no `pom.xml` para:

```xml
<mainClass>br.com.pedromagno.MainKt</mainClass>
```

---

## Estado atual da implementação

O projeto ainda está em fase inicial e alguns pontos ainda estão em evolução.

Atualmente, o motor de simulação possui fila de eventos por prioridade, mas ainda não possui uma modelagem completa de tempo discreto com `delay` ativo. No código atual, há trechos comentados indicando que o suporte a tempo de evento já foi pensado e deve ser reativado ou redesenhado nas próximas versões.

Também existem exemplos usando `Timer` e `TimerTask`, que funcionam com tempo real da JVM. No futuro, esses comportamentos podem ser integrados ao tempo virtual do `Environment`, deixando a simulação mais determinística.

---

## Roadmap sugerido

### Núcleo da simulação

- [x] Criar `Environment`
- [x] Criar `SimEvent`
- [x] Criar fila de eventos com prioridade
- [x] Criar `EventBus`
- [x] Criar `SimEntity`
- [x] Criar classe `Simulation`
- [ ] Reativar suporte a tempo virtual por evento
- [ ] Implementar `delay` no agendamento
- [ ] Criar controle de execução até determinado tempo
- [ ] Separar melhor tempo virtual e tempo real

### Entidades e processos

- [x] Criar entidades simuladas básicas
- [x] Permitir comunicação por eventos
- [ ] Criar API de processo simulável
- [ ] Avaliar integração com Kotlin Coroutines
- [ ] Criar eventos periódicos baseados no tempo virtual

### Recursos de simulação

- [ ] Criar recursos compartilhados
- [ ] Criar filas
- [ ] Criar stores
- [ ] Criar métricas de simulação
- [ ] Criar geração de relatórios

### Exemplos

- [x] Exemplo drone-radar-central
- [ ] Exemplo de fila simples
- [ ] Exemplo de rede de sensores
- [ ] Exemplo de veículo em movimento
- [ ] Exemplo de comunicação MQTT simulada

### Qualidade

- [ ] Adicionar testes unitários para `Environment`
- [ ] Adicionar testes unitários para `EventBus`
- [ ] Adicionar testes para ordenação de eventos
- [ ] Adicionar CI/CD
- [ ] Publicar documentação da API

---

## Possíveis aplicações

O SimKT pode ser usado como base de estudo ou prototipação para:

- simulação de sensores;
- sistemas de detecção;
- radares e sonares simulados;
- redes de comunicação;
- sistemas distribuídos;
- veículos autônomos;
- cenários de defesa;
- Internet das Coisas;
- filas e processos logísticos.

---

## Convenção dos eventos atuais

Alguns eventos usados nos exemplos:

```text
drone_detectado
drone_rastreado
sensor_posicionado
```

Esses nomes são usados pelo `EventBus` para conectar produtores e consumidores de eventos.

---

## Próximos passos recomendados

Uma boa evolução imediata para o projeto seria alterar o `Environment` para voltar a considerar tempo virtual.

Exemplo desejado:

```kotlin
env.schedule(delay = 5.0) {
    println("Tempo $now: evento executado")
}
```

Com isso, o `SimEvent` poderia voltar a ter um campo `time`:

```kotlin
data class SimEvent(
    val id: Long,
    val time: Double,
    val priority: Int,
    val action: Environment.() -> Unit
)
```

E a fila poderia ser ordenada por:

1. tempo do evento;
2. prioridade;
3. ordem de criação.

Isso aproximaria o SimKT de um motor real de simulação de eventos discretos.

---

## Autor

Desenvolvido por Pedro Magno.

---

## Licença

Este projeto ainda não possui uma licença definida.
