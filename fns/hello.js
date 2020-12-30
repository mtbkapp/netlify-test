exports.handler = async function(event, context) {
  const c = context.clientContext;
  console.log('clientContext', c);

  return {
    statusCode: 200,
    body: JSON.stringify({ message: "hello from netlify" })
  };
}

