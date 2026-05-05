package br.com.b2list.controller;


import br.com.b2list.domain.dto.ReprocessParkingLotRequest;
import br.com.b2list.service.ParkingLotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messaging/parking-lot/reprocess")
public class ParkingLotController {

    @Autowired
    private ParkingLotService parkingLotService;

    @PostMapping
    public ResponseEntity<?> reprocessParkingLot(@RequestBody ReprocessParkingLotRequest reprocessParkingLotRequest) {

        int reprocessedCount = parkingLotService.reprocessMessages(
                reprocessParkingLotRequest.getQueue(),
                reprocessParkingLotRequest.getMaxMessages()
        );

        return ResponseEntity.ok(reprocessedCount + " mensagens reprocessadas.");
    }
}
