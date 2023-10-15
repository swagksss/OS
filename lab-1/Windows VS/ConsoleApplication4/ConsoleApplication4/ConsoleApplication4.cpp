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
        perror("������� ����������� Winsock");
        return 1;
    }

    // ��������� �����
    int clientSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (clientSocket == -1) {
        perror("������� ��������� ������");
        return 1;
    }

    // ����������� ������ �� ���� �������
    sockaddr_in serverAddress;
    serverAddress.sin_family = AF_INET;
    serverAddress.sin_port = htons(8888); // ���� 8888
    serverAddress.sin_addr.s_addr = inet_addr("127.0.0.1"); // ������ ������� (localhost)



    // ϳ���������� �� �������
    if (connect(clientSocket, (struct sockaddr*)&serverAddress, sizeof(serverAddress)) == -1) {
        perror("������� ���������� �� �������");
        closesocket(clientSocket);
        return 1;
    }
    else
    {
        cout << "������ ���������� �� �������" << endl;

        const int bufferSize = 128;
        char inputBuffer[bufferSize]; 

        cout << "������ �������: ";
        cin.getline(inputBuffer, bufferSize); 

        const char* command = inputBuffer;
        // ��������� ������� "cancel" �� ������ ��� ���������� ���������
        size_t bytesSent = send(clientSocket, command, strlen(command), 0);

        if (bytesSent == -1) {
            perror("������� ��� �������� ������� �� ������");
            closesocket(clientSocket);
            return 1;
        }

        cout << "������ ���������� �������." << endl;

        // ��������� ����� �� ��������
        closesocket(clientSocket);
    }

   
    return 0;
}
