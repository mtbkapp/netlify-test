exports.handler = async function(event, context) {
  console.log('hello!');
  console.log(process.env);
  console.log(process.env.DB_API_KEY);

  return {
    statusCode: 200,
    body: JSON.stringify({ message: "hello from netlify" })
  };
}


