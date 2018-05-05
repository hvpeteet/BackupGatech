#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include "rtp.h"

/* GIVEN Function:
 * Handles creating the client's socket and determining the correct
 * information to communicate to the remote server
 */
CONN_INFO* setup_socket(char* ip, char* port){
	struct addrinfo *connections, *conn = NULL;
	struct addrinfo info;
	memset(&info, 0, sizeof(struct addrinfo));
	int sock = 0;

	info.ai_family = AF_INET;
	info.ai_socktype = SOCK_DGRAM;
	info.ai_protocol = IPPROTO_UDP;
	getaddrinfo(ip, port, &info, &connections);

	/*for loop to determine corr addr info*/
	for(conn = connections; conn != NULL; conn = conn->ai_next){
		sock = socket(conn->ai_family, conn->ai_socktype, conn->ai_protocol);
		if(sock <0){
			if(DEBUG)
				perror("Failed to create socket\n");
			continue;
		}
		if(DEBUG)
			printf("Created a socket to use.\n");
		break;
	}
	if(conn == NULL){
		perror("Failed to find and bind a socket\n");
		return NULL;
	}
	CONN_INFO* conn_info = malloc(sizeof(CONN_INFO));
	conn_info->socket = sock;
	conn_info->remote_addr = conn->ai_addr;
	conn_info->addrlen = conn->ai_addrlen;
	return conn_info;
}

void shutdown_socket(CONN_INFO *connection){
	if(connection)
		close(connection->socket);
}

/*
 * ===========================================================================
 *
 *			STUDENT CODE STARTS HERE. PLEASE COMPLETE ALL FIXMES
 *
 * ===========================================================================
 */


/*
 *  Returns a number computed based on the data in the buffer.
 */
static int checksum(char *buffer, int length){

	/*  ----  FIXME  ----
	 *
	 *  The goal is to return a number that is determined by the contents
	 *  of the buffer passed in as a parameter.  There a multitude of ways
	 *  to implement this function.  For simplicity, simply sum the ascii
	 *  values of all the characters in the buffer, and return the total.
	 */
	 int checksum = 0;
	 for (int i = 0; i < length; i++) {
	 	checksum ^= buffer[i];
	 }
	 return checksum;
}

/*
 *  Converts the given buffer into an array of PACKETs and returns
 *  the array.  The value of (*count) should be updated so that it
 *  contains the length of the array created.
 */
static PACKET* packetize(char *buffer, int length, int *count){

	/*  ----  FIXME  ----
	 *  The goal is to turn the buffer into an array of packets.
	 *  You should allocate the space for an array of packets and
	 *  return a pointer to the first element in that array.  Each
	 *  packet's type should be set to DATA except the last, as it
	 *  should be LAST_DATA type. The integer pointed to by 'count'
	 *  should be updated to indicate the number of packets in the
	 *  array.
	 */
	 PACKET* packets = calloc(sizeof(PACKET), length);
	 if (NULL == packets) {
	 	printf("malloc failed when creating buffer for packets\n");
	 	exit(1);
	 }
	 int final_payload_length = length % MAX_PAYLOAD_LENGTH;
	 int num_packets;
	 if (final_payload_length == 0) {
	 	num_packets = length / MAX_PAYLOAD_LENGTH;
	 } else {
	 	num_packets = length / MAX_PAYLOAD_LENGTH + 1;
	 }
	 // Set all but last packet.
	 for (int i = 0; i < num_packets - 1; i++) {
	 	packets[i].payload_length = MAX_PAYLOAD_LENGTH;
	 	packets[i].checksum = checksum(&(buffer[i * MAX_PAYLOAD_LENGTH]), MAX_PAYLOAD_LENGTH);
	 	memcpy(packets[i].payload, &(buffer[i * MAX_PAYLOAD_LENGTH]), sizeof(char) * MAX_PAYLOAD_LENGTH);
	 }
	 // Set last packet
	 packets[num_packets-1].type = LAST_DATA;
	 if (final_payload_length == 0) {
	 	packets[num_packets-1].payload_length = MAX_PAYLOAD_LENGTH;
	 	packets[num_packets-1].checksum = checksum(&(buffer[(num_packets - 1) * MAX_PAYLOAD_LENGTH]), MAX_PAYLOAD_LENGTH);
	 	memcpy(packets[num_packets-1].payload, &(buffer[(num_packets - 1) * MAX_PAYLOAD_LENGTH]), sizeof(char) * MAX_PAYLOAD_LENGTH);
	 } else {
	 	packets[num_packets-1].payload_length = final_payload_length;
	 	packets[num_packets-1].checksum = checksum(&(buffer[(num_packets - 1) * MAX_PAYLOAD_LENGTH]), final_payload_length);
	 	memcpy(packets[num_packets-1].payload, &(buffer[(num_packets - 1) * MAX_PAYLOAD_LENGTH]), sizeof(char) * final_payload_length);
	 }
	 *count = num_packets;
	 return packets;
}

/*
 * Send a message via RTP using the connection information
 * given on UDP socket functions sendto() and recvfrom()
 */
int rtp_send_message(CONN_INFO *connection, MESSAGE* msg){
	/* ---- FIXME ----
	 * The goal of this function is to turn the message buffer
	 * into packets and then, using stop-n-wait RTP protocol,
	 * send the packets and re-send if the response is a NACK.
	 * If the response is an ACK, then you may send the next one
	 */
	 int num_packets;
	 PACKET* packets = packetize(msg->buffer, msg->length, &num_packets);
	 PACKET* next_packet = packets;
	 PACKET resp_packet;
	 resp_packet.payload_length = 0;
	 resp_packet.checksum = 0;

	 int packet_sent = 0;
	 for (int i = 0; i < num_packets; i++) {
	 	packet_sent = 0;
	 	while(!packet_sent) {
	 		// printf("Sending: %s\nSize:%d\n", next_packet->payload, next_packet->payload_length);
		 	sendto(connection->socket, next_packet, sizeof(PACKET), 0, connection->remote_addr, connection->addrlen);
		 	recvfrom(connection->socket, &resp_packet, sizeof(PACKET), 0, connection->remote_addr, &(connection->addrlen));
		 	if (resp_packet.type == ACK) {
		 		// printf("got ack\n");
		 		packet_sent = 1;
		 	} else {
		 		// printf("got nack\n");
		 	}
		 }
		 next_packet++;
	 }
	 free(packets);

	 return 1;
}

/*
 * Receive a message via RTP using the connection information
 * given on UDP socket functions sendto() and recvfrom()
 */
MESSAGE* rtp_receive_message(CONN_INFO *connection){
	/* ---- FIXME ----
	 * The goal of this function is to handle
	 * receiving a message from the remote server using
	 * recvfrom and the connection info given. You must
	 * dynamically resize a buffer as you receive a packet
	 * and only add it to the message if the data is considered
	 * valid. The function should return the full message, so it
	 * must continue receiving packets and sending response
	 * ACK/NACK packets until a LAST_DATA packet is successfully
	 * received.
	 */
	 char* message_buffer = NULL;
	 int message_buffer_length = 1;

	 int message_length = 0;

	 PACKET next_packet;
	 PACKET resp_packet;
	 resp_packet.payload_length = 0;
	 resp_packet.checksum = 0;

	 unsigned char done = 0;
	 while(!done) {
	 	recvfrom(connection->socket, &next_packet, sizeof(PACKET), 0, connection->remote_addr, &(connection->addrlen));
	 	if (next_packet.checksum == checksum(next_packet.payload, next_packet.payload_length)) {
	 		message_length += next_packet.payload_length;
	 		if (message_length > message_buffer_length) {
	 			message_buffer_length = message_buffer_length * 2 + message_length;
	 			char* new_message_buffer = realloc(message_buffer, message_buffer_length);
	 			if (NULL == new_message_buffer) {
	 				fprintf(stderr, "realloc failed\n");
	 				fprintf(stderr, "here is your data %s\n", message_buffer);
	 				exit(1);
	 			}
	 			message_buffer = new_message_buffer;
	 		}
	 		memcpy(message_buffer + message_length - next_packet.payload_length, next_packet.payload, next_packet.payload_length);
	 		if (next_packet.type == LAST_DATA) {
	 			done = 1;
	 		}
	 		resp_packet.type = ACK;
	 	} else {
	 		resp_packet.type = NACK;
	 	}
	 	sendto(connection->socket, &resp_packet, sizeof(PACKET), 0, connection->remote_addr, connection->addrlen);
	 }
	 MESSAGE* message = malloc(sizeof(MESSAGE));
	 if (NULL == message) {
	 	fprintf(stderr, "malloc failed when creating message");
	 	exit(1);
	 }
	 char* final_buffer = realloc(message_buffer, message_length + 1);
	 if (NULL == final_buffer) {
	 	fprintf(stderr, "realloc failed when creating final buffer");
	 	exit(1);
	 }
	 final_buffer[message_length] = '\0';
	 message->length = message_length + 1;
	 message->buffer = final_buffer;
	 return message;
}
