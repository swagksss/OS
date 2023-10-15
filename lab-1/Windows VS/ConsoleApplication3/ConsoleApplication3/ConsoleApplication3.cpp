#include <iostream>
#include <thread>
#include <chrono>
#include <winsock2.h>
#include <ws2tcpip.h>

#pragma comment(lib, "Ws2_32.lib")


using namespace std;

// ������� ��� ���������� f(x)
double calculateF(double x) {
    return x * x;
}

// ������� ��� ���������� g(x)
double calculateG(double x) {
    return sin(x);
}

int main() {
    setlocale(LC_ALL, "Ukrainian");

    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        perror("������� ����������� Winsock");
        return 1;
    }

    // ��������� �����
    int serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (serverSocket == -1) {
        perror("������� ��������� ������");
        return 1;
    }

    // ����������� ������ �� ���� ��� ������
    sockaddr_in serverAddress;
    serverAddress.sin_family = AF_INET;
    serverAddress.sin_port = htons(8888); // ���� 8888
    serverAddress.sin_addr.s_addr = INADDR_ANY;

    // ��'����� ����� � �������
    if (bind(serverSocket, (struct sockaddr*)&serverAddress, sizeof(serverAddress)) == -1) {
        perror("������� ��'�������� ������");
        closesocket(serverSocket);
        return 1;
    }

    // ������� �����
    if (listen(serverSocket, 5) == -1) {
        perror("������� �������� ������");
        closesocket(serverSocket);
        return 1;
    }

    cout << "������ ������� �������� ����������." << endl;

    // ������� ���������� �볺���
    sockaddr_in clientAddress;
    socklen_t clientAddressSize = sizeof(clientAddress);
    int clientSocket = accept(serverSocket, (struct sockaddr*)&clientAddress, &clientAddressSize);

    if (clientSocket == -1) {
        perror("������� ��������� ����������");
        closesocket(serverSocket);
        return 1;
    }

    // ����������� ������ ��� ���������� ���������
    double resultF = 0.0;
    double resultG = 0.0;
    bool cancelRequested = false;

    // �������� ���� ��������
    while (true) {
        // ������������� "select" ��� �������� ���� �� �����
        fd_set readSet;
        FD_ZERO(&readSet);
        FD_SET(clientSocket, &readSet);

        struct timeval timeout;
        timeout.tv_sec = 1; // ������ ��� ���������� 1 �������
        timeout.tv_usec = 0;

        int readySockets = select(clientSocket + 1, &readSet, NULL, NULL, &timeout);

        if (readySockets == -1) {
            perror("������� ��� ����������� select");
            break;
        }
        else if (readySockets == 0) {
            // ³�������� ���� �� �����, ���������� ����������
            if (!cancelRequested) {
                /*resultF = calculateF(x);
                resultG = calculateG(x);*/
            }
        }
        else {
            // � ���� �� ����� (����� �� ����������)
            char buffer[32];
            size_t bytesRead = recv(clientSocket, buffer, sizeof(buffer), 0);
            if (bytesRead <= 0) {
                break;
            }

            buffer[bytesRead] = '\0';

            if (strcmp(buffer, "cancel") == 0) {
                cancelRequested = true;
                cout << "��������� ����������." << endl;
            }
            else {
                double x = atof(buffer);

                resultF = calculateF(x);
                resultG = calculateG(x);

                cancelRequested = false;
            }
        }

        if (!cancelRequested) {
            // �������� ���������� ��������� f �� g
            double expressionResult = resultF + resultG;

            if (expressionResult == 0)
            {
                
            }
            else
            {
                cout << "��������� ������: " << expressionResult << endl;
            }
        }
        else {
            cout << "���������� ��������." << endl;
        }
    }

    // ��������� ������ �� ��������
    closesocket(clientSocket);
    closesocket(serverSocket);
    return 0;
}
