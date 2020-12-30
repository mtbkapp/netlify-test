exports.handler = async function(event, context) {
  const {identity, user} = context.clientContext;
  console.log('user', user);
  console.log(Object.keys(process.env).sort());

  return {
    statusCode: 200,
    body: JSON.stringify({ message: "hello from netlify" })
  };
}

