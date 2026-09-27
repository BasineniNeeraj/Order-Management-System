# 📦 Order Management System (OMS) - Backend REST API

A scalable, secure, and production-ready **Order Management System** built with **Java 21**, **Spring Boot 4.0.5**, **Spring Security (OAuth2 / Stateless JWT)**, **Spring Data JPA**, and **MySQL**, integrated with **Razorpay Payment Gateway**.

This project models real-world e-commerce backend operations including role-based access control, catalog management with soft deletes, high-concurrency order placement with optimistic locking, and resilient payment lifecycle verification.

## 🚀 Key Architectural & Engineering Highlights**:  
**Stateless Authentication & RBAC**: Implemented stateless JWT token-based authentication using Spring Security OAuth2 Resource Server and Nimbus JOSE, isolating customer and admin endpoint authorization (ROLE_USER vs. ROLE_ADMIN).  
**Concurrency & Race Condition Prevention**: Mitigated lost updates and phantom inventory depletion during simultaneous checkouts using JPA Optimistic Locking (@Version) on the Product entity.  
**Database Optimization & N+1 Prevention**: Solved the Hibernate N+1 select query bottleneck in multi-tiered order retrieval by designing custom JOIN FETCH queries across Order, OrderItem, and Product graphs.  
**Resilient Payment Lifecycle Management**: Integrated Razorpay API with server-side signature verification. Designed custom transaction propagation (@Transactional(noRollbackFor = PaymentVerificationException.class)) to ensure failed payment audits persist even during runtime failures.  
**Fault-Tolerant Catalog Design**: Implemented soft-deletion routines on products to maintain relational data integrity with historical orders and line items.  
**Centralized API Contracts & Error Responses**: Standardized JSON responses using Java records and centralized exception handling (@RestControllerAdvice) covering HTTP 400, 403, 404, and 409 status codes.  

## 🛠️ Tech Stack & Tools  
**Language**: Java 21  
**Framework**: Spring Boot 4.0.5  
**Security**: Spring Security 6, OAuth2 Resource Server, Nimbus JWT, BCrypt  
**Persistence & ORM**: Spring Data JPA, Hibernate, MySQL 8  
**API Documentation**: OpenAPI 3 / Swagger UI  
**Payment Integration**: Razorpay Java SDK  
**Build Tool**: Maven  
**Utilities**: Lombok, Jakarta Validation  

## 📐 System Architecture & ER Overview  
[ Client / Postman / Swagger UI]  
              │  
              ▼  
[ Spring Security Filter Chain ]  
  ├── Basic Auth (Token Issuance)  
  └── JWT Bearer Token Filter  
              │  
              ▼  
[ Controller Layer ]  
(Admin & Customer Endpoints)  
              │  
              ▼  
[ Service Layer ]  
(Business Logic, Transaction Boundaries)   
              │  
┌─────────────┴─────────────┐  
▼                           ▼  
[ Spring Data JPA ]       [ Razorpay Gateway ]  
           │                     (Orders & Verification)  
           ▼  
[ MySQL Database ]  
(Users, Products, Orders, OrderItems, Payments)  

## Relational Schema Summary  
User 1 : N Order  
Order 1 : N OrderItem N : 1 Product  
Order 1 : 1 Payment  

## 📑 API Endpoints Summary  
1. Authentication — 1 API  
**POST**    /authenticate    Authenticate user/admin and generate JWT  

2. Customer User — 5 APIs  
**POST**	  /customer/users/register	          Register customer  
**GET**	    /customer/users/profile	            Get logged-in user's profile  
**PUT**	    /customer/users/profile	            Update logged-in user's profile  
**PATCH**  	/customer/users/profile/password	  Change logged-in user's password  
**DELETE**	/customer/users/profile            	Delete logged-in user's account  

3. Admin User — 4 APIs  
**POST**	  /admin/users/register    Create admin  
**GET**	    /admin/users  	         Get all users  
**GET**	    /admin/users/{id}	       Get user by ID  
**DELETE**	/admin/users/{id}	       Delete user  

4. Customer Product — 2 APIs  
**GET**    	/customer/products        Get all products  
**GET**	    /customer/products/{id}	  Get product by ID  

5. Admin Product — 5 APIs  
**POST**	  /admin/products	          Create product  
**GET**	    /admin/products	          Get all products  
**GET**	    /admin/products/{id}	    Get product by ID  
**PUT**	    /admin/products/{id}	    Update product  
**DELETE**	/admin/products/{id}	    Delete product  

6. Customer Order — 4 APIs  
**POST**	  /customer/orders	                  Create order  
**GET**	    /customer/orders/user	              Get logged-in user's orders  
**GET**	    /customer/orders/{orderId}	        Get own order by ID  
**PATCH**	  /customer/orders/{orderId}/cancel	  Cancel own order  

7. Admin Order — 4 APIs  
**GET**	    /admin/orders	                      Get all orders  
**GET**	    /admin/orders/{orderId}	            Get order by ID  
**GET**	    /admin/orders/user/{userId}	        Get orders for a specific user  
**PATCH**	  /admin/orders/{orderId}/status	    Update order status  

8. Customer Payment — 3 APIs  
**POST**	  /customer/payments/orders/{orderId}	  Create Razorpay payment/order  
**POST**	  /customer/payments/verify             Verify Razorpay payment  
**GET**	    /customer/payments/{paymentId}	      Get customer's payment  

9. Admin Payment — 2 APIs  
**GET**	    /admin/payments	              Get all payments  
**GET**	    /admin/payments/{paymentId}	  Get payment by ID  

## ⚙️ Local Setup & Installation  
**Prerequisites**  
Java 21 or later installed  
Maven 3.8+  
MySQL 8.0+ running locally  
Free Razorpay Account - https://accounts.razorpay.com (Test Mode)  

## 1. Clone the Repository  
git clone https://github.com/<your-username>/order-management-system.git  
cd order-management-system  

## 2. Configure Database & Secrets  
**Create a MySQL database**  
CREATE DATABASE omsprojectdb;  

Update your src/main/resources/application.properties (or supply environment variables):  
spring.datasource.url=jdbc:mysql://localhost:3306/omsprojectdb?useSSL=false&serverTimezone=UTC  
spring.datasource.username=root  
spring.datasource.password=root  
spring.jpa.hibernate.ddl-auto=update  
spring.jpa.show-sql=true  
spring.jpa.properties.hibernate.format_sql=true  

spring.jackson.mapper.accept-case-insensitive-enums=true  

razorpay.key.id=YOUR_RAZORPAY_TEST_KEY_ID  
razorpay.key.secret=YOUR_RAZORPAY_TEST_KEY_SECRET  

## 3. Build & Run Application  
mvn clean install  
mvn spring-boot:run  
The application will start on http://localhost:8080  

## 📖 Interactive Documentation & UI  
Swagger UI Documentation: http://localhost:8080/swagger-ui/index.html  
OpenAPI Raw JSON: http://localhost:8080/v3/api-docs  
Test Payment Checkout UI: Open http://localhost:8080/payment.html in your browser to test the end-to-end checkout flow using test card or UPI credentials.  
