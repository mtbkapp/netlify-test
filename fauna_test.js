var faunadb = require('faunadb'), q = faunadb.query;
var secret = process.env.FAUNA_SECRET;
var adminClient = new faunadb.Client({ secret: secret });


function createUser() {
  var user = {
    data: {
      name: 'player 1'
    },
    credentials: {
      password: 'Password'
    }
  };
  var createUser = q.Create(q.Collection('login_test'), user);

  adminClient.query(createUser)
  .then((ret) => console.log(ret))
  .catch((ret) => console.error(ret));
}


async function loginUser() {
  var client = adminClient; 
  var ref = q.Ref(q.Collection("login_test"), "286357188828463624");

  return await client.query(q.Login(ref, { password: 'Password' }))
}


loginUser()
  .then((x) => console.log("done", x))
  .catch((e)=> console.log("error", e));
