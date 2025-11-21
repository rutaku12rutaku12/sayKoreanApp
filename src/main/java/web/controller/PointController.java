package web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import web.model.mapper.PointMapper;


@RestController
@RequiredArgsConstructor
@RequestMapping("saykorean")
public class PointController {

    private final PointMapper pointMapper;

    @GetMapping("/store/point")
    public int getMyPoint(@RequestParam int userNo) {
        return pointMapper.getTotalPoint(userNo);
    }

}
