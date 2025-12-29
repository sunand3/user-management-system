# GCP Assignment — User Upload & Migration

## Overview

This project demonstrates integration between **Google Cloud Datastore** and **BigQuery** using a Java web application.  
It allows you to:

1. Upload a user Excel file (`Users.xlsx`) containing user data.
2. Validate and store user details in **Datastore**.
3. Test login functionality using the uploaded credentials.
4. Migrate the stored user data to **BigQuery**.

---

## Features

- Upload and parse Excel file (`.xlsx`) containing user records.
- Store records in **Google Cloud Datastore**.
- User authentication (email + password).
- Data migration from **Datastore → BigQuery**.
- Simple UI dashboard to view, search, filter, and delete user records.

---

## Problem Statement

Create a **Google Standard App Engine Java Project** to manage user data. The system must populate data in **Google Cloud Datastore** and migrate that data to **BigQuery** programmatically.  
The UI serves as a comprehensive management dashboard for these records.

---

## Milestone 1: Data Ingestion & User Management

1. **Excel Upload**: Upload Excel files containing user data.
2. **User Attributes**: Name, DOB, Email, Password, Phone, Gender, Address.
3. **Datastore Persistence**: Store user records in a Datastore Kind `User`.
4. **User Directory UI**:
    - Display all users with key attributes.
    - Search & filter users via native JS.
    - Delete a user record from the UI and Datastore.
5. **Authentication**: Users can login with credentials from uploaded Excel.

---

## Milestone 2: BigQuery Migration

1. **Migration UI**: Page displaying imported user records with option to trigger migration.
2. **Bulk Migration**: Migrate all users from Datastore to BigQuery table `User`.
3. **Data Integrity**: Ensure BigQuery reflects the same data as Datastore.
4. **Scale**: Populate ~100 records for demonstration.

---

## Architecture & Constraints

- Servlet-based architecture (or minimal REST library like Jersey)
- **No frameworks**: No Spring Boot, Hibernate, React, Angular, etc.
- **Native JS**: Plain modular JavaScript for UI and API interactions.
- **Storage**: Google Cloud Datastore for application data, BigQuery for migration.

---

## Prerequisites

- Java 11+
- Maven 3+
- Google Cloud Project with:
    - App Engine API
    - Datastore API
    - BigQuery dataset
- Google Cloud SDK (`gcloud`) installed locally

---

## Build & Run Instructions

```bash
# 1. Build project using Maven
mvn clean package

# 2. Authenticate with GCP
gcloud auth application-default login

# 3. Deploy WAR to Tomcat locally
copy target/*.war C:\Users\apache-tomcat-9.0.112\webapps
cd C:\Users\apache-tomcat-9.0.112\bin
startup.bat
