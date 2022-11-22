import socket 
import json
import numpy as np
import matplotlib.pyplot as plot
HOST = '192.168.1.102'     # IP address
PORT = 65431            # Port to listen on (use ports > 1023)
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.bind((HOST, PORT))
    s.listen()
    print("Starting server at: ", (HOST, PORT))
    conn, addr = s.accept()
    with conn:
        print("Connected at", addr)
        while True:
            data = conn.recv(1024).decode('utf-8')
            print(data)
            # if (data.count('{') != 1):
            #     # Incomplete data are received.
            #     choose = 0
            #     buffer_data = data.split('}')
            #     while buffer_data[choose][0] != '{':
            #         choose += 1
            #     data = buffer_data[choose] + '}'
                
            # obj = json.loads(data)
            # t = obj['s']
            # print(t)