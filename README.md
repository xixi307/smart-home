# ChatModel Spring AI

基于 Spring AI 和 OpenAI 的聊天模型服务，提供多种 AI 对话功能，包括普通聊天、情感分析、RAG（检索增强生成）、流式响应等。

## 🚀 技术栈

- **Java 21** - 编程语言
- **Spring Boot 3.5.3** - 应用框架
- **Spring AI 1.0.0** - AI 集成框架
- **OpenAI GPT-4o-mini** - 大语言模型
- **Maven** - 项目管理工具
- **SpringDoc OpenAPI** - API 文档

## ✨ 主要功能

### 1. 基础聊天功能
- **普通聊天** (`POST /api/ai/chat`) - 基本的对话交互
- **模板聊天** (`POST /api/ai/chat-with-prompt`) - 使用提示词模板的对话
- **系统提示聊天** (`POST /api/ai/chat-with-system-prompt`) - 带系统角色设定的对话

### 2. 高级功能
- **情感分析** (`POST /api/ai/sentiment/analyze`) - 分析文本情感（正面/负面/讽刺）
- **向量嵌入** (`POST /api/ai/embedding-client-conversion`) - 文本向量化
- **结构化输出** (`GET /api/ai/output`) - 生成结构化的演员电影列表
- **RAG 检索增强** (`POST /api/ai/rag`) - 基于 restaurant 数据的检索增强生成
- **流式响应** (`POST /api/ai/chat/stream`) - 实时流式聊天响应

## 📋 前置要求

- JDK 21 或更高版本
- Maven 3.6+
- Git

## 🛠️ 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/xixi307/smart-home.git
cd smart-home/chatmodel-springai
```

### 2. 配置应用

编辑 `src/main/resources/application.properties` 文件：

```properties
# 服务器端口
server.port=8081

# OpenAI 配置（当前使用演示服务，无需 API Key）
spring.ai.openai.api-key=demo
spring.ai.openai.base-url=http://langchain4j.dev/demo/openai
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.chat.options.temperature=0.7
```

> **注意**: 如需使用真实的 OpenAI API，请替换为你的 API Key 和正确的 base URL。

### 3. 构建项目

```bash
mvn clean install
```

### 4. 运行应用

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8081` 启动。

## 📖 API 使用示例

### 访问 Swagger UI

启动应用后，访问 [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) 查看完整的 API 文档。

### 1. 普通聊天

```bash
curl -X POST http://localhost:8081/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"query": "你好，请介绍一下你自己"}'
```

### 2. 情感分析

```bash
curl -X POST http://localhost:8081/api/ai/sentiment/analyze \
  -H "Content-Type: application/json" \
  -d '{"query": "这个餐厅的服务真是太棒了！"}'
```

### 3. RAG 检索增强

```bash
curl -X POST http://localhost:8081/api/ai/rag \
  -H "Content-Type: application/json" \
  -d '{"query": "推荐一家意大利餐厅"}'
```

### 4. 流式聊天

```bash
curl -X POST http://localhost:8081/api/ai/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"query": "讲一个笑话"}'
```

### 5. 获取演员电影列表

```bash
curl "http://localhost:8081/api/ai/output?actor=Jr%20NTR"
```

## 📁 项目结构

```
chatmodel-springai/
├── src/
│   ├── main/
│   │   ├── java/com/example/ai/
│   │   │   ├── config/              # 配置类
│   │   │   │   ├── CustomClientHttpResponse.java
│   │   │   │   ├── LoggingConfig.java
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── controller/          # 控制器
│   │   │   │   └── ChatController.java
│   │   │   ├── model/               # 数据模型
│   │   │   │   ├── request/         # 请求模型
│   │   │   │   │   └── AIChatRequest.java
│   │   │   │   └── response/        # 响应模型
│   │   │   │       ├── AIChatResponse.java
│   │   │   │       ├── AIStreamChatResponse.java
│   │   │   │       └── ActorsFilms.java
│   │   │   ├── service/             # 业务逻辑
│   │   │   │   └── ChatService.java
│   │   │   └── ChatModelApplication.java
│   │   └── resources/
│   │       ├── data/
│   │       │   └── restaurants.json  # RAG 数据源
│   │       ├── application.properties
│   │       └── rag-prompt-template.st
│   └── test/                        # 测试代码
├── pom.xml
└── README.md
```

## 🔧 配置说明

### 使用真实 OpenAI API

如果要使用真实的 OpenAI API，修改 `application.properties`：

```properties
spring.ai.openai.api-key=your-api-key-here
spring.ai.openai.base-url=https://api.openai.com/v1
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.chat.options.temperature=0.7
```

### 日志配置

当前日志级别设置为 INFO，可以在 `application.properties` 中调整：

```properties
logging.level.org.apache.hc.client5.http=INFO
logging.level.com.example.ai=DEBUG
```

## 🧪 测试

运行单元测试：

```bash
mvn test
```

## 📝 代码格式化

项目使用 Spotless 进行代码格式化：

```bash
mvn spotless:apply  # 自动格式化代码
mvn spotless:check  # 检查代码格式
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目采用 MIT 许可证。

## 👤 作者

- **xixi307** - [GitHub](https://github.com/xixi307)

## 🙏 致谢

- [Spring AI](https://spring.io/projects/spring-ai)
- [OpenAI](https://openai.com/)
- [Spring Boot](https://spring.io/projects/spring-boot)
