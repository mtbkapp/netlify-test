var faunadb = require('faunadb');
var secret = process.env.FAUNA_SECRET;
var client = new faunadb.Client({ secret: secret });


