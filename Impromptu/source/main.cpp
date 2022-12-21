/* Sockets Example
 * Copyright (c) 2016-2020 ARM Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "arm_math.h"
#include "math_helper.h"
#include "mbed-trace/mbed_trace.h"
#include "mbed.h"
#include "stm32l475e_iot01_accelero.h"
#include "stm32l475e_iot01_gyro.h"
#include "stm32l475e_iot01_hsensor.h"
#include "stm32l475e_iot01_magneto.h"
#include "stm32l475e_iot01_psensor.h"
#include "stm32l475e_iot01_tsensor.h"
#include "wifi_helper.h"
#include <cstdint>
#include <cstdio>
#include <cstdlib>

#if MBED_CONF_APP_USE_TLS_SOCKET
#include "root_ca_cert.h"

#ifndef DEVICE_TRNG
#error "mbed-os-example-tls-socket requires a device which supports TRNG"
#endif
#endif // MBED_CONF_APP_USE_TLS_SOCKET
#define SNR_THRESHOLD_F32 75.0f
#define BLOCK_SIZE 32
#define NUM_TAPS_ARRAY_SIZE 29
#define NUM_TAPS 29
#define TEST_LENGTH_SAMPLES 160

AnalogIn f1(PC_5);
AnalogIn f2(PC_4);
AnalogIn f3(PC_3);
AnalogIn f4(PC_2);
AnalogIn f5(PC_1);

class SocketDemo {
  static constexpr size_t MAX_NUMBER_OF_ACCESS_POINTS = 20;
  static constexpr size_t MAX_MESSAGE_RECEIVED_LENGTH = 100;

#if MBED_CONF_APP_USE_TLS_SOCKET
  static constexpr size_t REMOTE_PORT = 443; // tls port
#else
  static constexpr size_t REMOTE_PORT = 8080; // standard HTTP port
#endif // MBED_CONF_APP_USE_TLS_SOCKET
public:
  SocketDemo() : _net(NetworkInterface::get_default_instance()) {}

  ~SocketDemo() {
    if (_net) {
      _net->disconnect();
    }
  }

  void run() {
    if (!_net) {
      printf("Error! No network interface found.\r\n");
      return;
    }

    /* if we're using a wifi interface run a quick scan */
    if (_net->wifiInterface()) {
      /* the scan is not required to connect and only serves to show visible
       * access points */
      wifi_scan();

      /* in this example we use credentials configured at compile time which are
       * used by NetworkInterface::connect() but it's possible to do this at
       * runtime by using the WiFiInterface::connect() which takes these
       * parameters as arguments */
    }

    /* connect will perform the action appropriate to the interface type to
     * connect to the network */

    printf("Connecting to the network...\r\n");

    nsapi_size_or_error_t result = _net->connect();
    if (result != 0) {
      printf("Error! _net->connect() returned: %d\r\n", result);
      return;
    }

    print_network_info();

    /* opening the socket only allocates resources */
    result = _socket.open(_net);
    if (result != 0) {
      printf("Error! _socket.open() returned: %d\r\n", result);
      return;
    }

#if MBED_CONF_APP_USE_TLS_SOCKET
    result = _socket.set_root_ca_cert(root_ca_cert);
    if (result != NSAPI_ERROR_OK) {
      printf("Error: _socket.set_root_ca_cert() returned %d\n", result);
      return;
    }
    _socket.set_hostname(MBED_CONF_APP_HOSTNAME);
#endif // MBED_CONF_APP_USE_TLS_SOCKET

    /* now we have to find where to connect */

    SocketAddress address;

    if (!resolve_hostname(address)) {
      return;
    }

    address.set_port(REMOTE_PORT);

    /* we are connected to the network but since we're using a connection
     * oriented protocol we still need to open a connection on the socket */

    printf("Opening connection to remote port %d\r\n", REMOTE_PORT);

    result = _socket.connect(address);
    if (result != 0) {
      printf("Error! _socket.connect() returned: %d\r\n", result);
      return;
    }
    BSP_ACCELERO_Init();
    printf("Demo concluded successfully \r\n");
    // Mode 1
    // int16_t pDataXYZ[3] = {0};
    // int16_t prev=0;
    // int16_t sample_num = 0;
    // int16_t pitch=0;
    // while (1) {
    //     unsigned short f1v=f1.read_u16();
    //     unsigned short f2v=f2.read_u16();
    //     unsigned short f3v=f3.read_u16();
    //     unsigned short f4v=f4.read_u16();
    //     unsigned short f5v=f5.read_u16();
    //     BSP_ACCELERO_AccGetXYZ(pDataXYZ);
    //     // printf("%d\n",pDataXYZ[1]);
    //     if(pitch==0&&pDataXYZ[1]<-500&&prev>500)
    //         pitch=1;
    //     else if(pitch==0&&pDataXYZ[1]>500&&prev<-500)
    //         pitch=2;
    //     // else if(pDataXYZ[1]-prev<500&&pDataXYZ[1]-prev>-500)
    //     else
    //         pitch=0;
    //     prev=pDataXYZ[1];
    //     printf("%d %d %d %d %d %d\n",f1v,f2v,f3v,f4v,f5v,pitch);
    //     ThisThread::sleep_for(50);
    //     char buffer[10];
    //     nsapi_size_t bytes_to_send=sprintf(buffer, "%d %d %d %d %d
    //     %d\n",f1v>14000,f2v>14000,f3v>14000,f4v>14000,f5v>14000,pitch);
    //     nsapi_size_or_error_t bytes_sent = 0;

    //     while (bytes_to_send) {

    //         bytes_sent = _socket.send(buffer + bytes_sent, bytes_to_send);
    //         if (bytes_sent < 0) {
    //             printf("Error! _socket.send() returned: %d\r\n", bytes_sent);
    //         } else {

    //         }
    //         bytes_to_send -= bytes_sent;
    //         // printf("%d\n",bytes_to_send);
    //         ThisThread::sleep_for(50);
    //     }
    // }
    // Mode 2
    float32_t *inputF32, *outputF32;
    int16_t pDataXYZ[3] = {0};
    float32_t pYSeq[TEST_LENGTH_SAMPLES] = {0};
    static float32_t testOutput[TEST_LENGTH_SAMPLES];
    static float32_t firStateF32[BLOCK_SIZE + NUM_TAPS - 1];
    int pitch = 0;
    const float32_t firCoeffs32[29] = {
        -0.0018225230f, -0.0015879294f, +0.0000000000f, +0.0036977508f,
        +0.0080754303f, +0.0085302217f, -0.0000000000f, -0.0173976984f,
        -0.0341458607f, -0.0333591565f, +0.0000000000f, +0.0676308395f,
        +0.1522061835f, +0.2229246956f, +0.2504960933f, +0.2229246956f,
        +0.1522061835f, +0.0676308395f, +0.0000000000f, -0.0333591565f,
        -0.0341458607f, -0.0173976984f, -0.0000000000f, +0.0085302217f,
        +0.0080754303f, +0.0036977508f, +0.0000000000f, -0.0015879294f,
        -0.0018225230f};
    arm_fir_instance_f32 S;
    arm_status status;
    outputF32 = &testOutput[0];
    uint32_t blockSize = BLOCK_SIZE;
    uint32_t numBlocks = TEST_LENGTH_SAMPLES / BLOCK_SIZE;

    float32_t snr;
    arm_fir_init_f32(&S, NUM_TAPS, (float32_t *)&firCoeffs32[0],
                     &firStateF32[0], blockSize);
    bool flag = false;
    while (1) {
        flag=false;
      for (int i = 0; i < TEST_LENGTH_SAMPLES; i++) {
        BSP_ACCELERO_AccGetXYZ(pDataXYZ);
        pYSeq[i] = (float32_t)pDataXYZ[1];
        ThisThread::sleep_for(1);
      }
      for (int i = 0; i < numBlocks; i++) {
        arm_fir_f32(&S, pYSeq + (i * blockSize), outputF32 + (i * blockSize),
                    blockSize);
      }
      unsigned short f1v = f1.read_u16();
      unsigned short f2v = f2.read_u16();
      unsigned short f3v = f3.read_u16();
      unsigned short f4v = f4.read_u16();
      unsigned short f5v = f5.read_u16();
      for (int i = 1; i < TEST_LENGTH_SAMPLES-1; i++) {
        // printf("%f ",testOutput[i]);
        if (pitch == 0 && testOutput[i] < -30 && testOutput[i - 1] > 30) {
          pitch = 1;
          flag = true;
        //   break;
        } else if (pitch == 0 && testOutput[i] > 35 &&
                   testOutput[i - 1] < -35) {
          pitch = 2;
          flag = true;
        //   break;
        }
        
      }
    //   printf("\n");
      if (!flag)
        pitch = 0;
      //   prev = pDataXYZ[1];
      printf("%d %d %d %d %d %d\n", f1v, f2v, f3v, f4v, f5v, pitch);
    //   ThisThread::sleep_for(50);
      char buffer[16];
      nsapi_size_t bytes_to_send =
          sprintf(buffer, "%d %d %d %d %d %d\n", f1v > 14000, f2v > 14000,
                  f3v > 14000, f4v > 14000, f5v > 14000, pitch);
      nsapi_size_or_error_t bytes_sent = 0;

      while (bytes_to_send) {
        bytes_sent = _socket.send(buffer + bytes_sent, bytes_to_send);
        if (bytes_sent < 0) {
          printf("Error! _socket.send() returned: %d\r\n", bytes_sent);
        } else {
        }
        bytes_to_send -= bytes_sent;
        // printf("%d\n",bytes_to_send);
        // ThisThread::sleep_for(50);
      }
    }
  }

private:
  bool resolve_hostname(SocketAddress &address) {
    const char hostname[] = MBED_CONF_APP_HOSTNAME;

    /* get the host address */
    printf("\nResolve hostname %s\r\n", hostname);
    nsapi_size_or_error_t result = _net->gethostbyname(hostname, &address);
    if (result != 0) {
      printf("Error! gethostbyname(%s) returned: %d\r\n", hostname, result);
      return false;
    }

    printf("%s address is %s\r\n", hostname,
           (address.get_ip_address() ? address.get_ip_address() : "None"));

    return true;
  }

  bool send_http_request() {
    /* loop until whole request sent */
    const char buffer[] = "GET / HTTP/1.1\r\n"
                          "Host: ifconfig.io\r\n"
                          "Connection: close\r\n"
                          "\r\n";

    nsapi_size_t bytes_to_send = strlen(buffer);
    nsapi_size_or_error_t bytes_sent = 0;

    printf("\r\nSending message: \r\n%s", buffer);

    while (bytes_to_send) {
      bytes_sent = _socket.send(buffer + bytes_sent, bytes_to_send);
      if (bytes_sent < 0) {
        printf("Error! _socket.send() returned: %d\r\n", bytes_sent);
        return false;
      } else {
        printf("sent %d bytes\r\n", bytes_sent);
      }

      bytes_to_send -= bytes_sent;
    }

    printf("Complete message sent\r\n");

    return true;
  }

  bool receive_http_response() {
    char buffer[MAX_MESSAGE_RECEIVED_LENGTH];
    int remaining_bytes = MAX_MESSAGE_RECEIVED_LENGTH;
    int received_bytes = 0;

    /* loop until there is nothing received or we've ran out of buffer space */
    nsapi_size_or_error_t result = remaining_bytes;
    while (result > 0 && remaining_bytes > 0) {
      result = _socket.recv(buffer + received_bytes, remaining_bytes);
      if (result < 0) {
        printf("Error! _socket.recv() returned: %d\r\n", result);
        return false;
      }

      received_bytes += result;
      remaining_bytes -= result;
    }

    /* the message is likely larger but we only want the HTTP response code */

    printf("received %d bytes:\r\n%.*s\r\n\r\n", received_bytes,
           strstr(buffer, "\n") - buffer, buffer);

    return true;
  }

  void wifi_scan() {
    WiFiInterface *wifi = _net->wifiInterface();

    WiFiAccessPoint ap[MAX_NUMBER_OF_ACCESS_POINTS];

    /* scan call returns number of access points found */
    int result = wifi->scan(ap, MAX_NUMBER_OF_ACCESS_POINTS);

    if (result <= 0) {
      printf("WiFiInterface::scan() failed with return value: %d\r\n", result);
      return;
    }

    printf("%d networks available:\r\n", result);

    for (int i = 0; i < result; i++) {
      printf("Network: %s secured: %s BSSID: %hhX:%hhX:%hhX:%hhx:%hhx:%hhx "
             "RSSI: %hhd Ch: %hhd\r\n",
             ap[i].get_ssid(), get_security_string(ap[i].get_security()),
             ap[i].get_bssid()[0], ap[i].get_bssid()[1], ap[i].get_bssid()[2],
             ap[i].get_bssid()[3], ap[i].get_bssid()[4], ap[i].get_bssid()[5],
             ap[i].get_rssi(), ap[i].get_channel());
    }
    printf("\r\n");
  }

  void print_network_info() {
    /* print the network info */
    SocketAddress a;
    _net->get_ip_address(&a);
    printf("IP address: %s\r\n",
           a.get_ip_address() ? a.get_ip_address() : "None");
    _net->get_netmask(&a);
    printf("Netmask: %s\r\n", a.get_ip_address() ? a.get_ip_address() : "None");
    _net->get_gateway(&a);
    printf("Gateway: %s\r\n", a.get_ip_address() ? a.get_ip_address() : "None");
  }

private:
  NetworkInterface *_net;

#if MBED_CONF_APP_USE_TLS_SOCKET
  TLSSocket _socket;
#else
  TCPSocket _socket;
#endif // MBED_CONF_APP_USE_TLS_SOCKET
};

int main() {
  printf("\r\nStarting socket demo\r\n\r\n");

#ifdef MBED_CONF_MBED_TRACE_ENABLE
  mbed_trace_init();
#endif

  SocketDemo *example = new SocketDemo();
  MBED_ASSERT(example);
  example->run();

  return 0;
}
