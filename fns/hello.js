

exports.handler = async function(event, context) {
  var faunadb = require('faunadb');
  console.log('hello!', faunadb);

  return {
    statusCode: 200,
    body: JSON.stringify({ message: "hello from netlify" })
  };
}


