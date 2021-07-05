const express = require('express')
const app = express()
const path = require('path');
const port = 3000

app.get('/static/code.js', (req, res) => {
    res.sendFile(path.join(__dirname + '/static/code.js'))
})

app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname + '/index.html'))
})

app.listen(port, () => {
    console.log(`Example app listening at http://localhost:${port}`)
})