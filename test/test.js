var http  = require('http');
var clone = require('clone');

var base  = '/api/lp';
var count = 0;

var options = {
  host: 'localhost',
  port: 8081,
  path: null,
  method: 'GET',
  headers: {
    'Accept-Encoding': 'gzip, deflate',
  },
};

var process = function(handler, encoding) {
  return function(response) {
    if(encoding)
    {
      response.setEncoding(encoding);
    }

    var data = [];
    response.on('data', function(chunk) {
      data.push(chunk);
    });

    response.on('end', function() {
      var buffer = Buffer.concat(data);
      handler(response, buffer);
    });
  }
};

var onSend = function(response, buffer) {
	var body  = buffer.toString('utf8');
	var reply = JSON.parse(body);
	console.log(reply.value);
};

var onConnect = function(response, buffer) {
	var body  = buffer.toString('utf8');
	var reply = JSON.parse(body);
	if(reply.type == 'OK')
	{
		var session = reply.sessionId;
		var data    = JSON.stringify({'class': 'xavante.Reverse', 'value': 'sample value'});

		var       op = clone(options);
		op.method    = 'POST';
		op.path      = base + '/snd/' + session + '/' + (++count);

		var req = http.request(op, process(onSend));
		req.write(data);
		req.end();
	}
	else
	{
		throw 'Connection Refused: ' + body;
	}
};

var op = clone(options);
op.path = base + '/con';

http.request(op, process(onConnect)).end();

