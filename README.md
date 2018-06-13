# react-native-http-bridge

A fork of [react-native-http-bridge](https://github.com/alwx/react-native-http-bridge).

HTTP Server for [React Native](https://github.com/facebook/react-native)

Supports POST and GET-requests.

## Install

```shell
npm install --save react-native-http-bridge
```

or 

```shell
yarn add react-native-http-bridge
```

## Automatically link

#### With React Native 0.27+

```shell
react-native link react-native-http-bridge
```

## Example

First import/require react-native-http-server:

```js

    import httpBridge from '@kingstinct/react-native-http-bridge';

```


Initalise the server in the `componentWillMount` lifecycle method. You need to provide a `port` and a callback where requests will be captured. Currently there is no way to return responses.

```js

    componentWillMount(){

      // initalize the server (now accessible via localhost:1234)
      httpBridge.start(5561, function(request) {

          // request.url
          // request.postData

          //do something with the data
        return { code: 200, data: { hello: 'world' } }; //or return a promise
      });

    }

```

Finally, ensure that you disable the server when your component is being unmounted.

```js

  componentWillUnmount() {
    httpBridge.stop();
  }

```
