Order and Stock Manager

1.Technologies Used
Java 15 – The main programming language.
Maven – For dependency management and building the project.
MySQL – The database used to persist data (products, orders, order details).
RabbitMQ – The message broker used to receive order requests in JSON format and send responses.
XML – XML files provide the product stock data; they are read from the input directory (e.g., "stock_inbox") and then copied to the archive directory (e.g., "stock_done").
Swing – The graphical user interface of the application is built using Swing, utilizing a JTabbedPane to separate product management from order management.
JAXB – For deserializing XML files into Java objects.
Jackson – For deserializing/serializing JSON messages that represent orders.

2.Functionalities

Stock Import:
XML files (containing a list of products with details such as id, name, quantity, and price) are read from the "stock_inbox" directory.
Products from the XML are added to the database. If a product already exists, its quantity is updated (stock values are added).
Processed XML files are copied to the "stock_done" directory for archiving.

Order Processing:
The application receives orders in JSON format via the RabbitMQ "ORDERS" queue.
The messages are deserialized into Order objects that contain information about the customer and the list of ordered products.
For each order, the system checks if the available stock is sufficient. If yes, the stock is updated and the order status is set to "RESERVED"; if not, the status becomes "NO_STOCK".
The order is stored in the database, and a response message (with the updated status) is published to the "ORDERS_RESPONSE" queue.

User Interface (Swing):
"Products" Tab: Allows adding, listing, and deleting products from the database.
"Orders" Tab: Displays orders from the database (with their associated products), and allows editing orders and adding new ones.
A dedicated panel (the orderStatusArea text area) shows the real-time status of processed orders (e.g., updates from RabbitMQ).

3.Project Structure
Model:
Product and Products – Represent stock data from XML.
Order – Represents customer orders.

Repository:
ProductRepository – Manages CRUD operations for products (insertion, updating, deletion, listing).
OrderRepository – Manages inserting, updating, and listing orders. Orders are associated with products through the intermediate table "order_products".

Processor:
StockProcessor – Processes XML files from "stock_inbox" and updates the database.
OrderProcessor – Listens for messages on the RabbitMQ "ORDERS" queue, processes orders, and publishes responses on "ORDERS_RESPONSE".

Connector:
DatabaseConnector – Handles establishing the connection to the database.
RabbitMQConnector (if used separately) – Establishes the connection to RabbitMQ.

Interface:
Main.java – Launches the Swing UI and starts the stock and order processing tasks.

4.Design Aspects
DAO Pattern: Repositories separate the data access logic from the business logic.
Resource Management: New connections are created for each operation using DatabaseConnector, avoiding the reuse of closed connections.
Concurrency: A pool of 5 threads is used in the OrderProcessor to concurrently process orders from RabbitMQ.
Centralized Configuration: An "application.properties" file (located in src/main/resources) centralizes settings for the database, RabbitMQ, and the paths to the stock directories.
