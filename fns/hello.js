exports.handler = async function(event, context) {
  const {identity, user} = context.clientContext;
  console.log('identity', identity);
  console.log('user', user);

  return {
    statusCode: 200,
    body: JSON.stringify({ message: "hello from netlify" })
  };
}

