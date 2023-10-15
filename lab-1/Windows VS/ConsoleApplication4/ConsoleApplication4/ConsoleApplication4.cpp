#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <iostream>
#include <winsock2.h>
#include <ws2tcpip.h>

#pragma comment(lib, "Ws2_32.lib")

using namespace std;

int main() {
    setlocale(LC_ALL, "Ukrainian");

    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        perror("Помилка ініціалізації Winsock");
        return 1;
    }

    // Створюємо сокет
    int clientSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (clientSocket == -1) {
        perror("Помилка створення сокету");
        return 1;
    }

    // Налаштовуємо адресу та порт сервера
    sockaddr_in serverAddress;
    serverAddress.sin_family = AF_INET;
    serverAddress.sin_port = htons(8888); // Порт 8888
    serverAddress.sin_addr.s_addr = inet_addr("127.0.0.1"); // Адреса сервера (localhost)



    // Підключаємося до сервера
    if (connect(clientSocket, (struct sockaddr*)&serverAddress, sizeof(serverAddress)) == -1) {
        perror("Помилка підключення до сервера");
        closesocket(clientSocket);
        return 1;
    }
    else
    {
        cout << "Успішне підключення до сервера" << endl;

        const int bufferSize = 128;
        char inputBuffer[bufferSize]; 

        cout << "Введіть команду: ";
        cin.getline(inputBuffer, bufferSize); 

        const char* command = inputBuffer;
        // Надсилаємо команду "cancel" на сервер для скасування обчислень
        size_t bytesSent = send(clientSocket, command, strlen(command), 0);

        if (bytesSent == -1) {
            perror("Помилка при відправці команди на сервер");
            closesocket(clientSocket);
            return 1;
        }

        cout << "Успішно відправлено команду." << endl;

        // Закриваємо сокет та виходимо
        closesocket(clientSocket);
    }

   
    return 0;
}
