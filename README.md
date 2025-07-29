# Variomedia API Integration 🌐📦

This Spring Boot application automatically connects to the [Variomedia API](https://www.variomedia.de/) and retrieves customer, domain, and DNS record data — all without opening a browser or requiring any manual steps. Upon launch, the application immediately begins parsing data and generates a ready-to-use output file.

## ✨ Key Features

- ⚡ **Automatic Parsing**: Starts fetching and processing data as soon as the application launches — no browser, no clicks needed.
- 🔐 **Secure API Access**: Uses token-based authentication.
- 👥 **Customer Overview**: Fetches all customer profiles.
- 🌐 **Domain Insights**: Lists all domains per customer.
- 🛡️ **DNS Records**: Displays full DNS configuration for each domain.
- 📁 **Auto-Saved Output**: Parsed results are saved directly into a structured file (e.g., JSON or CSV) for immediate use.
- 🛠️ **Zero Configuration Needed**: All endpoints and tokens are managed via `application.properties`.


## ⚙️ Technologies Used

- Java 21
- Spring Boot
- Spring Web (RestTemplate)
- Jackson (ObjectMapper)
- Maven

## 🔧 Configuration

In your `application.properties` (excluded from version control):

properties
variomedia.api.url=https://api.variomedia.de/v1
variomedia.api.token=your_secure_token_here

## 🚀 How to Run

mvn spring-boot:run
The service is now ready to interact with the Variomedia API endpoints internally.

## 📂 Output
The final data is saved in a structured format (JSON or CSV) — ready for further use or analysis.

## 📄 License
This project is **private** and intended for internal use in a real-world production environment. Redistribution or public sharing is not permitted.

## 👤 Author
Vladislav Bondarevs