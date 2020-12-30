exports.handler = async function(event, context) {
  console.log('context', context);

  return {
    statusCode: 200,
    body: JSON.stringify({ message: "hello from netlify" })
  };
}

