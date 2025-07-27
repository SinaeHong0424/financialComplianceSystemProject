Project Overview

A comprehensive compliance tracking and management system designed for the New York State Department of Financial Services (DFS).

This system enables DFS compliance officers to efficiently monitor and manage financial institutions under their supervision.

Purpose

Track and monitor financial institutions regulated by DFS including:

Banks – Regional and community banks

Insurance Companies – Property, casualty, and life insurance

Money Service Businesses (MSB) – Money transmitters and currency exchanges

FinTech Companies – Digital payment and cryptocurrency platforms

Credit Unions – Member-owned financial cooperatives

Broker-Dealers – Securities and investment firms

Key Features

Phase 1: Core Functionality (In Progress)
[ ] Financial entity registration and tracking
[ ] Compliance status management (Compliant, Under Investigation, etc.)
[ ] Violation recording with severity levels
[ ] Risk assessment (Low, Medium, High, Critical)
[ ] Comprehensive audit trail
[ ] Automated alerting system

Phase 2: Advanced Features (Planned)
[ ] Dashboard with real-time analytics
[ ] REST API for external integrations
[ ] Oracle APEX user interface
[ ] Advanced reporting and trend analysis
[ ] Salesforce CRM integration
[ ] Document management system

Technology Stack

Layer | Technology | Version
Database | Oracle Database | 25c Free
Backend | Java (Jakarta EE) | 11
Frontend | Oracle APEX | 24.1
Build Tool | Maven | 3.9+
Testing | JUnit + Mockito | 5.9+
API | REST with Gson | -
Logging | SLF4J + Logback | 2.0.7
Connection Pool | Oracle UCP | 23.3


Project Structure
dfs-compliance-system/
├── database/              
│   ├── 01_create_schema.sql      
│   ├── 02_create_packages.sql    
│   └── 03_insert_sample_data.sql 
├── src/
│   ├── main/
│   │   ├── java/        
│   │   │   └── com/dfs/compliance/
│   │   └── resources/    
│   └── test/
│       └── java/        
├── apex/                 
├── docs/                 
│   ├── REQUIREMENTS.md           
│   ├── PROJECT_PLAN.md           
│   ├── DATABASE_DESIGN.md        
│   ├── ER_DIAGRAM.txt            
│   ├── DATA_DICTIONARY.md        
│   ├── TECH_STACK_EVALUATION.md  
│   └── ARCHITECTURE.md           
├── config/               
├── pom.xml              
└── README.md            
