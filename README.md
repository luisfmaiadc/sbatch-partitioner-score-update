<h1 align="center">sbatch-partitioner-score-update</h1>

<p align="center" style="margin-bottom: 20;">
  <img src="https://img.shields.io/badge/java-25-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25" />
  <img src="https://img.shields.io/badge/spring%20boot-4.0.5-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot 4" />
  <img src="https://img.shields.io/badge/spring%20batch-6-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Batch 6" />
  <img src="https://img.shields.io/badge/mysql-8-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/jdbc-%23007396.svg?style=for-the-badge" alt="JDBC" />
  <img src="https://img.shields.io/badge/lombok-CA0C0C?style=for-the-badge&logo=lombok&logoColor=white" alt="Lombok" />
  <img src="https://img.shields.io/badge/apache%20maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
</p>

<p align="center">
Aplicação Batch desenvolvida com <b>Java 25</b>, <b>Spring Boot 4</b> e <b>Spring Batch 6</b> com foco em <b>Particionamento Local (Local Partitioning)</b> para atualização em massa de registros.
</p>

---

<h2>📌 Visão Geral</h2>

<p align="justify">
O projeto simula um cenário comum em sistemas corporativos: <b>reprocessamento e atualização em massa de usuários</b>, recalculando score e redefinindo status com base em regras de negócio.
</p>

<p align="justify">
O objetivo principal é demonstrar, na prática, como o <b>Particionamento Local no Spring Batch</b> pode reduzir drasticamente o tempo de execução em workloads de grande volume, explorando paralelismo via múltiplas threads.
</p>

<p align="justify">
O experimento foi realizado com <b>1.000.000 de registros</b>, permitindo medir ganho real de throughput.
</p>

---

<h2>🚀 Tecnologias Utilizadas</h2>

* **Java 25**
* **Spring Boot 4.0.5**
* **Spring Batch 6**
* **JDBC**
* **MySQL 8**
* **Lombok**
* **Apache Maven**

---

<h2>⚙️ Estratégia de Processamento</h2>

O Job é estruturado utilizando o padrão **Master/Slave Step** com Particionamento Local.

<h3>🧵 Arquitetura</h3>

```
Job
└── Master Step
    └── Slave Step (executado em múltiplas partições)
```


<h3>🔹 Estratégia de Particionamento</h3>

O particionamento é realizado por **range de ID**, dividindo a tabela em múltiplos blocos independentes.

Exemplo conceitual:

- Partição 1 → ID 1–125000
- Partição 2 → ID 125001–250000
- ...
- Partição 8 → ID 875001–1000000

Cada partição executa o mesmo Step em paralelo, utilizando:

- `Partitioner`
- `TaskExecutor`
- `@StepScope` para injetar `minId` e `maxId`

---

<h2>🔄 Regras de Negócio Aplicadas</h2>

Durante o processamento:

1. Penalidade de score para usuários inativos há mais de 180 dias
2. Bônus para usuários com score elevado
3. Reclassificação automática de status:
    - **BRONZE** → score &lt; 300
    - **SILVER** → 300 ≤ score ≤ 700
    - **GOLD** → score &gt; 700
4. Atualização do campo `last_update`

O processamento é CPU + I/O bound, envolvendo cálculos e atualização massiva no banco.

---

<h2>📊 Benchmark de Performance</h2>

Teste realizado com <b>1.000.000 registros</b>.

| Estratégia | Tempo Total |
|------------|------------|
| Single Thread | **29m23s365ms** |
| 8 Threads (Partitioning) | **5m53s308ms** |

<p align="justify">
🚀 O uso de Particionamento Local resultou em uma redução de aproximadamente <b>5x no tempo total de execução</b>.
</p>

<p align="justify">
O experimento evidencia como paralelismo controlado pode impactar diretamente o throughput em workloads de grande volume.
</p>

---

<h2>🗄️ Estrutura de Banco de Dados</h2>

<h3>Banco de Dados</h3>

- `dbUserScore` → Dados de domínio
- `sbatch_execution` → Metadados do Spring Batch

<h3>Tabela Principal</h3>

```sql
CREATE TABLE TbUsers (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     name VARCHAR(150) NOT NULL,
     score DECIMAL(10,2) NOT NULL DEFAULT 0,
     status VARCHAR(20) NOT NULL,
     last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tb_users_status ON TbUsers(status);
```

<h3>População com 1.000.000 de Registros</h3>

Utiliza CTE recursiva para geração em massa de dados sintéticos.

```sql
SET SESSION cte_max_recursion_depth = 1000000;

INSERT INTO TbUsers (name, score, status, last_update)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000000
)
SELECT
    CONCAT('User_', n),
    ROUND(RAND() * 1000, 2),
    CASE
        WHEN RAND() < 0.33 THEN 'BRONZE'
        WHEN RAND() < 0.66 THEN 'SILVER'
        ELSE 'GOLD'
        END,
    NOW() - INTERVAL FLOOR(RAND() * 365) DAY
FROM seq;
```

---

<h2>🏗️ Estrutura do Projeto</h2>

```
sbatch-partitioner-score-update
│-- src/main/java/com/portfolio/luisfmdc/sbatch_partitioner_score_update
│   ├── config/
│   │    ├── job/
│   │    ├── step/
│   │    ├── partitioner/
│   │    ├── reader/
│   │    ├── processor/
│   │    └── writer/
│   └── AppConfig
│   ├── domain/
│   └── Application
│-- src/main/resources
│   ├── database/
│   │    ├── create-database-and-table.sql
│   │    └── populate-table.sql
│   └── application.yml
```

---

<h2>⚙️ Configuração</h2>

<h3>📌 application.yml</h3>

- `chunk-size`: 5000
- `thread-usage`: 8
- `HikariCP max pool size`: 12
- `rewriteBatchedStatements=true` para otimização de batch update

---

<h2>🛠️ Execução</h2>

<h3>Pré-requisitos</h3>

- Java 25
- Maven
- MySQL 8 rodando localmente

<h3>Variáveis de Ambiente</h3>

Defina:

```
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

Caso não definidas, podem ser utilizadas credenciais padrão locais.

<h3>Passos</h3>

```bash
git clone https://github.com/luisfmaiadc/sbatch-partitioner-score-update.git
cd sbatch-partitioner-score-update

mvn clean install
mvn spring-boot:run
```

---

<h2>📚 Aprendizados Consolidados</h2>

<ul> 
  <li><b>Particionamento Local no Spring Batch</b></li> 
  <li>Master/Slave Step Architecture</li> 
  <li>Range-based partitioning strategy</li> 
  <li>Performance tuning com HikariCP</li> 
  <li>Impacto de chunk-size em throughput</li> 
  <li>Análise prática de concorrência e gargalos</li> 
  <li>Comparação real entre execução single-thread e multi-thread</li> 
</ul>

<h2>🎯 Objetivo do Projeto</h2>

Consolidar conhecimentos avançados em:

- Processamento em massa com Spring Batch
- Paralelismo controlado
- Performance tuning
- Arquitetura orientada a throughput
- Identificação de gargalos reais

<hr/> 

<p align="center">Desenvolvido por <b>Luis Felipe Maia da Costa</b></p>