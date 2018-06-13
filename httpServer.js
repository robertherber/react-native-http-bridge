/**
 * @providesModule react-native-http-server
 */


import { DeviceEventEmitter } from 'react-native';
import { NativeModules } from 'react-native';

const Server = NativeModules.HttpServer;

module.exports = {
  start(port, serviceName, callback) {
    if (port == 80) {
      throw 'Invalid server port specified. Port 80 is reserved.';
    }

    Server.start(port, serviceName);
    DeviceEventEmitter.addListener('httpServerResponseReceived', (args) => {
      console.log('args', args);
      const { requestId } = args;
      return Promise.resolve(callback(args)).then(({ code = 200, type = 'application/json', body, data }) => {
        Server.respond(code, type, data ? JSON.stringify(data) : body, requestId);
      })
      .catch((error) => {
        console.log('ERROR', error);
        return Server.respond(500, 'application/json', JSON.stringify({ message: 'An error occurred' }), requestId);
      });
    });
  },

  stop() {
    Server.stop();
    DeviceEventEmitter.removeListener('httpServerResponseReceived');
  },

  respond(code, type, body) {
    Server.respond(code, type, body);
  },
};
