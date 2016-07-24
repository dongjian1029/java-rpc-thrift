namespace java com.appchina.rpc.thrift.support

struct Response {
  1: i32 status,
  2: map<string, string> headers,
  3: optional binary body
}

struct Request {
  1: string path,
  2: map<string, string> headers,
  3: optional binary body
}

service ThriftServce {
	Response execute(1:Request request);
	bool ping();
}
