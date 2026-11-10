require("dotenv").config();
const { Client } = require("pg");

async function testConnection() {
    const client = new Client({ connectionString: process.env.DB_URL });
    try {
        await client.connect();
        const { rows } = await client.query("SELECT NOW()");
        console.log("Database connection successful:", rows[0]);
    } catch (err) {
        console.error("Database connection error:", err.message);
    } finally {
        await client.end();
    }
}

testConnection();
