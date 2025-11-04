# 🏫 Sistema de Agendamento - IFSP Itapetininga

Um sistema web desenvolvido em **Java Spring Boot** para agendamento da **Quadra**, **Estúdio** e **LAB Maker** do IFSP Itapetininga.  
O sistema permite o cadastro de usuários, login seguro, criação e edição de agendamentos, além de uma visualização intuitiva por meio de um **calendário interativo**.

---

## 🚀 Funcionalidades Principais

✅ **Autenticação de Usuário**  
- Login e cadastro com credenciais armazenadas no banco de dados.  
- Cada usuário só pode editar ou excluir **seus próprios agendamentos**.  

✅ **Agendamento Inteligente**  
- Evita conflitos de horário para o mesmo espaço.  
- Validação automática de datas e horários.  

✅ **Visualização dos Agendamentos**  
- Tela organizada listando todos os agendamentos futuros.  
- Agendamentos passados são ocultados automaticamente.  

✅ **Calendário Interativo**  
- Exibe os dias com agendamentos marcados por meio de um **★**.  
- Ao clicar em uma data, o usuário é levado diretamente à lista de agendamentos daquele dia.  

✅ **Interface Responsiva e Moderna**  
- Design leve, inspirado na identidade visual do IFSP.  
- Navegação fluida entre as telas.  

---

## 🧰 Tecnologias Utilizadas

| Camada | Tecnologias |
|--------|--------------|
| **Back-end** | Java 21 · Spring Boot 3.5.6 · Spring MVC · Spring Data JPA |
| **Front-end** | HTML5 · CSS3 · Thymeleaf |
| **Banco de Dados** | MySQL (ou H2 para testes locais) |
| **Ferramentas** | Maven · Lombok · Spring Boot DevTools |

---

## ⚙️ Como Executar o Projeto Localmente

### 1️⃣ Clonar o repositório
```bash
git clone https://github.com/seu-usuario/seu-repositorio.git
cd seu-repositorio

2️⃣ Configurar o banco de dados
Edite o arquivo src/main/resources/application.properties com suas credenciais:

properties
Copiar código
# Exemplo MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/agendamentos
spring.datasource.username=root
spring.datasource.password=suasenha
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
💡 Para testes rápidos, também é possível usar o banco em memória H2 (descomente / ajuste as propriedades correspondentes no application.properties).

3️⃣ Executar o projeto
Se estiver usando Spring Tool Suite (STS), IntelliJ ou VS Code com extensão Java, basta executar a classe principal:

java
Copiar código
com.ifsp.projeto.ProjetoApplication
Ou, via terminal:

bash
Copiar código
mvn spring-boot:run
O sistema ficará disponível em:
👉 http://localhost:8080

👥 Estrutura de Usuários
Tipo de Usuário	Permissões
Usuário comum	Criar, visualizar e editar apenas seus agendamentos
Administrador (opcional)	Pode visualizar/editar/excluir todos os agendamentos

📅 Navegação do Sistema
Tela de Login → autenticação e acesso.

Tela Inicial → botões para Agendar, Ver Agendamentos e Calendário.

Tela de Agendamentos → lista de compromissos futuros (agendamentos passados são ocultados).

Tela de Calendário → visão mensal com ★ em dias agendados; ao clicar, filtra os agendamentos daquele dia.

Tela Histórico → lista de agendamentos passados.

💾 Estrutura do Projeto
bash
Copiar código
src/
├── main/
│   ├── java/com/ifsp/projeto/
│   │   ├── controller/   # Controladores Spring MVC
│   │   ├── model/        # Entidades JPA
│   │   ├── repository/   # Interfaces JPA
│   │   └── service/      # Lógica de negócios (opcional)
│   └── resources/
│       ├── static/       # CSS, imagens, JS
│       ├── templates/    # Páginas Thymeleaf
│       └── application.properties
└── test/
📌 Observações importantes
Quebras de linha (CRLF / LF): no Windows o Git pode mostrar avisos sobre conversão de final de linha; isso não impede o funcionamento. Se quiser padronizar, configure git config core.autocrlf true.

Versões: o pom.xml do projeto define as versões (Spring Boot 3.5.6 etc.). Ajuste java.version caso precise usar JDK diferente.

Banco remoto (opcional): se for hospedar o banco (por ex. no Render ou outro serviço), atualize a URL/credenciais em application.properties e garanta que a porta/usuário/senha estejam corretos.

✅ Recomendações para o professor / avaliador
Ter JDK 17+ (recomendado JDK 21 conforme pom.xml) instalado.

Ter Maven instalado (mvn) — ou usar IDE com suporte a Maven.

Criar um banco MySQL chamado agendamentos (ou ajustar nome na application.properties).

Rodar mvn spring-boot:run e acessar http://localhost:8080.

🧑‍💻 Desenvolvido por
Lucas Henrique de Melo Campos Weiss
Projeto acadêmico para a disciplina de Programação Web - IFSP Itapetininga
Professor orientador: Gerson Carriel

🏁 Licença
Este projeto é de uso acadêmico e pode ser adaptado livremente para fins educacionais.
© 2025 IFSP Itapetininga