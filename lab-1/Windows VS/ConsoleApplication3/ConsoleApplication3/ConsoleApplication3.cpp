#include <iostream>
#include <thread>
#include <chrono>
#include <winsock2.h>
#include <ws2tcpip.h>

#pragma comment(lib, "Ws2_32.lib")


using namespace std;

// Функція для обчислення f(x)
double calculateF(double x) {
    return x * x;
}

// Функція для обчислення g(x)
double calculateG(double x) {
    return sin(x);
}

int main() {
    setlocale(LC_ALL, "Ukrainian");

    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        perror("Помилка ініціалізації Winsock");
        return 1;
    }

    // Створюємо сокет
    int serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (serverSocket == -1) {
        perror("Помилка створення сокету");
        return 1;
    }

    // Налаштовуємо адресу та порт для сокету
    sockaddr_in serverAddress;
    serverAddress.sin_family = AF_INET;
    serverAddress.sin_port = htons(8888); // Порт 8888
    serverAddress.sin_addr.s_addr = INADDR_ANY;

    // Зв'язуємо сокет з адресою
    if (bind(serverSocket, (struct sockaddr*)&serverAddress, sizeof(serverAddress)) == -1) {
        perror("Помилка зв'язування сокету");
        closesocket(serverSocket);
        return 1;
    }

    // Слухаємо сокет
    if (listen(serverSocket, 5) == -1) {
        perror("Помилка слухання сокету");
        closesocket(serverSocket);
        return 1;
    }

    cout << "Сервер готовий приймати підключення." << endl;

    // Очікуємо підключення клієнта
    sockaddr_in clientAddress;
    socklen_t clientAddressSize = sizeof(clientAddress);
    int clientSocket = accept(serverSocket, (struct sockaddr*)&clientAddress, &clientAddressSize);

    if (clientSocket == -1) {
        perror("Помилка прийняття підключення");
        closesocket(serverSocket);
        return 1;
    }

    // Ініціалізація змінних для результатів обчислень
    double resultF = 0.0;
    double resultG = 0.0;
    bool cancelRequested = false;

    // Основний цикл програми
    while (true) {
        // Використовуємо "select" для перевірки подій на сокеті
        fd_set readSet;
        FD_ZERO(&readSet);
        FD_SET(clientSocket, &readSet);

        struct timeval timeout;
        timeout.tv_sec = 1; // Задаємо час очікування 1 секунду
        timeout.tv_usec = 0;

        int readySockets = select(clientSocket + 1, &readSet, NULL, NULL, &timeout);

        if (readySockets == -1) {
            perror("Помилка при використанні select");
            break;
        }
        else if (readySockets == 0) {
            // Відсутність подій на сокеті, продовжуємо обчислення
            if (!cancelRequested) {
                /*resultF = calculateF(x);
                resultG = calculateG(x);*/
            }
        }
        else {
            // Є подія на сокеті (запит на скасування)
            char buffer[32];
            size_t bytesRead = recv(clientSocket, buffer, sizeof(buffer), 0);
            if (bytesRead <= 0) {
                break;
            }

            buffer[bytesRead] = '\0';

            if (strcmp(buffer, "cancel") == 0) {
                cancelRequested = true;
                cout << "Скасовано обчислення." << endl;
            }
            else {
                double x = atof(buffer);

                resultF = calculateF(x);
                resultG = calculateG(x);

                cancelRequested = false;
            }
        }

        if (!cancelRequested) {
            // Отримуємо результати обчислень f та g
            double expressionResult = resultF + resultG;

            if (expressionResult == 0)
            {
                
            }
            else
            {
                cout << "Результат виразу: " << expressionResult << endl;
            }
        }
        else {
            cout << "Обчислення скасовані." << endl;
        }
    }

    // Закриваємо сокети та виходимо
    closesocket(clientSocket);
    closesocket(serverSocket);
    return 0;
}
