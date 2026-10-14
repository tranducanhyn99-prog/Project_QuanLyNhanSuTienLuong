"""QA-only TCP relay; a pause file disconnects only clients of this relay."""
import argparse
import socket
import threading
import time
from pathlib import Path

p = argparse.ArgumentParser()
p.add_argument('--port', type=int, default=14513)
p.add_argument('--pause-file', required=True)
a = p.parse_args()
pause = Path(a.pause_file)
connections = set()
lock = threading.Lock()

def close_pair(pair):
    for s in pair:
        try:
            s.shutdown(socket.SHUT_RDWR)
        except OSError:
            pass
        s.close()
    with lock:
        connections.difference_update(pair)

def pipe(source, destination, pair):
    try:
        while data := source.recv(65536):
            destination.sendall(data)
    except OSError:
        pass
    finally:
        close_pair(pair)

listener = None
try:
    while True:
        if pause.exists():
            if listener:
                listener.close()
                listener = None
                with lock:
                    active = tuple(connections)
                close_pair(active)
                print('PAUSED: QA relay disconnected; SQL Server unchanged', flush=True)
            time.sleep(0.1)
            continue
        if listener is None:
            listener = socket.socket()
            listener.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            listener.bind(('127.0.0.1', a.port))
            listener.listen()
            listener.settimeout(0.2)
            print(f'READY: 127.0.0.1:{a.port} -> 127.0.0.1:1433', flush=True)
        try:
            client, _ = listener.accept()
        except socket.timeout:
            continue
        try:
            server = socket.create_connection(('127.0.0.1', 1433), timeout=5)
            server.settimeout(None)
        except OSError:
            client.close()
            continue
        pair = (client, server)
        with lock:
            connections.update(pair)
        threading.Thread(target=pipe, args=(client, server, pair), daemon=True).start()
        threading.Thread(target=pipe, args=(server, client, pair), daemon=True).start()
finally:
    if listener:
        listener.close()
    with lock:
        active = tuple(connections)
    close_pair(active)
