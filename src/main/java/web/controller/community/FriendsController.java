package web.controller.community;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.model.dto.community.FriendsDto;
import web.service.community.FriendsService;

import java.util.List;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendsController {
    private final FriendsService friendsService;

    //친구 요청
    @PostMapping("/add")
    public ResponseEntity<?> addFriend(@RequestParam int offer, @RequestParam int receiver){
        return ResponseEntity.ok(friendsService.addFriend(offer, receiver));
    }

    //친구 수락
    @PutMapping("/accept")
    public ResponseEntity<?> acceptFriend(@RequestParam int offer, @RequestParam int receiver){
        System.out.println("offer=" + offer + ", receiver=" + receiver);
        return ResponseEntity.ok(friendsService.acceptFriend(offer, receiver));
    }

    //친구 거절
    @DeleteMapping("/refusal")
    public ResponseEntity<?> refusalFriend(@RequestParam int offer, @RequestParam int receiver){
        return ResponseEntity.ok(friendsService.refusalFriend(offer, receiver));
    }

    //친구 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFriend(@RequestParam int offer, @RequestParam int receiver){
        return ResponseEntity.ok(friendsService.deleteFriend(offer, receiver));
    }

    //친구 차단
    @DeleteMapping("/block")
    public ResponseEntity<?> blockFriend(@RequestParam int offer, @RequestParam int receiver){
        return ResponseEntity.ok(friendsService.blockFriend(offer, receiver));
    }

    //요청 받은 목록 조회
    @GetMapping("/requests/recv")
    public ResponseEntity<List<FriendsDto>> requestsList(@RequestParam int userNo){
        return ResponseEntity.ok(friendsService.requestsList(userNo));
    }

    //내 친구 목록 조회
    @PutMapping("/list")
    public ResponseEntity<?> FriendsList(@RequestParam int userNo){
        return ResponseEntity.ok(friendsService.friendList(userNo));
    }
}
