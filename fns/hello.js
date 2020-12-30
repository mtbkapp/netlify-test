var faunadb = require('faunadb');

exports.handler = async function(event, context) {
  console.log('hello!', faunadb);

  return {
    statusCode: 200,
    body: JSON.stringify({ message: "hello from netlify" })
  };
}


