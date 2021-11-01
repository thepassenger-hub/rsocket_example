import { RSocketClient, JsonSerializer, IdentitySerializer } from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import { FlowableProcessor } from 'rsocket-flowable';


// backend ws endpoint
const wsURL = 'ws://localhost:7071';

const processor = new FlowableProcessor(sub => {})
// rsocket client
const client = new RSocketClient({
    serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer
    },
    setup: {
        keepAlive: 60000,
        lifetime: 180000,
        dataMimeType: 'application/json',
        metadataMimeType: 'message/x.rsocket.routing.v0',
    },
    transport: new RSocketWebSocketClient({
        url: wsURL
    })
});

// error handler
const errorHanlder = (e) => console.log(e);
// response handler
const responseHanlder = (payload) => {
    console.log(payload)
    

    // const li = document.createElement('li');
    // li.innerText = payload.data;
    // li.classList.add('list-group-item', 'small')
    // document.getElementById('result').appendChild(li);
}

const responseHanlderBP = async (payload) => {
    console.log(payload)
    function sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
      }
      console.log("before sleeping")
    await sleep(2000)
    console.log("after sleeping")
    // const li = document.createElement('li');
    // li.innerText = payload.data;
    // li.classList.add('list-group-item', 'small')
    // document.getElementById('result').appendChild(li);
}

// request to rsocket-websocket and response handling
const numberRequester = (socket, value) => {
    console.log(socket)
    console.log(value)
    socket.requestChannel({
        data: value,
        metadata: String.fromCharCode('andonio2'.length) + 'andonio2'
    }).subscribe({
        onError: errorHanlder,
        onNext: responseHanlder,
        onSubscribe: subscription => {
            subscription.request(100); // set it to some max value
        }
    })
}

const numberRequesterMono = (socket, value) => {
    console.log(socket)
    console.log(value)
    socket.requestResponse({
        data: value,
        metadata: String.fromCharCode('andonio'.length) + 'andonio'
    }).subscribe({
        onError: errorHanlder,
        // onNext: responseHanlder,
        onComplete: responseHanlder,
        onSubscribe: cancel => {
            // subscription.request(100); // set it to some max value
        }
    })
}

const numberRequester2 = (socket, processor) => {
    socket.requestChannel(processor.map(i => {
        console.log(`emitted ${i}`)
        return {
            data: i,
            metadata: String.fromCharCode('andonioBP'.length) + 'andonioBP'
        }
    })).subscribe({
        onError: errorHanlder,
        onNext: responseHanlderBP,
        onSubscribe: subscription => {
            subscription.request(100); // set it to some max value
        }
    })
}

const numberRequesterBP = (socket, value) => {
    socket.requestStream({
            data: value,
            metadata: String.fromCharCode('andonioBP'.length) + 'andonioBP'
    }).subscribe({
        onError: errorHanlder,
        onNext: responseHanlderBP,
        onSubscribe: subscription => {
            subscription.request(100); // set it to some max value
        }
    })
}

// once the backend connection is established, register the event listeners
// client.connect().then(socket => {
//     document.getElementById('n').addEventListener('change', ({srcElement}) => {
//         numberRequesterMono(socket, parseInt(srcElement.value));
//     })
// }, errorHanlder);

// client.connect().then(sock => {
//     numberRequester2(sock, processor);
//     document.getElementById('n').addEventListener('keyup', ({srcElement}) => {
//         if(srcElement.value.length > 0){
//             processor.onNext(parseInt(srcElement.value))
//         }
//     })
// }, errorHanlder);
client.connect().then(sock => {
    numberRequester2(sock, processor);
    document.getElementById('n').addEventListener('change', ({srcElement}) => {
        if(srcElement.value.length > 0){
            const out = [1,2,3,4,5,6,7,8,9,10]
            function sleep(ms) {
                return new Promise(resolve => setTimeout(resolve, ms));
              }
              
            // await sleep(500)
            out.forEach(async elem => {console.log("sleeping after emitting"); await sleep(500);processor.onNext(elem)})
        }
    })
}, errorHanlder);

// client.connect().then(sock => {
//     document.getElementById('n').addEventListener('change', ({srcElement}) => {
//             numberRequesterBP(sock, srcElement.value);

//     })
// }, errorHanlder);
