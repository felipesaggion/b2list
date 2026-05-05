package br.com.b2list.service;

public interface ParkingLotService {

    int reprocessMessages(String parkingLotQueueName, int maxMessages);
}
