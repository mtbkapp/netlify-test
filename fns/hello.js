var junk = require('netlify-fns/acme.mod_test.fauna');

exports.handler = async function(event, context) {

  return {
    statusCode: 200,
    body: JSON.stringify({ message: "hello from netlify" })
  };
}

console.log(junk.get_data());


