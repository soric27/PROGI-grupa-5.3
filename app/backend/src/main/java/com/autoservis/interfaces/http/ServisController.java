package com.autoservis.interfaces.http;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoservis.interfaces.dto.ServisInfoDto;
import com.autoservis.models.ServisInfo;
import com.autoservis.repositories.ServisInfoRepository;

@RestController
@RequestMapping("/api/servis")
public class ServisController {

    private final ServisInfoRepository repo;

    public ServisController(ServisInfoRepository repo) { this.repo = repo; }

    @GetMapping
    public ServisInfoDto get() {
        List<ServisInfo> all = repo.findAll();
        if (all.isEmpty()) {
            return new ServisInfoDto("info@autoservis.hr", "+38512345678", "Auto Servis MK2 - najbolji servisi u gradu. ");
        }
        ServisInfo s = all.get(0);
        return new ServisInfoDto(s.getContactEmail(), s.getContactPhone(), s.getAboutText());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> update(@RequestBody ServisInfoDto dto) {
        List<ServisInfo> all = repo.findAll();
        ServisInfo s;
        if (all.isEmpty()) {
            s = new ServisInfo(dto.contactEmail(), dto.contactPhone(), dto.aboutText());
        } else {
            s = all.get(0);
            s.setContactEmail(dto.contactEmail());
            s.setContactPhone(dto.contactPhone());
            s.setAboutText(dto.aboutText());
        }
        repo.save(s);
        return ResponseEntity.ok(new ServisInfoDto(s.getContactEmail(), s.getContactPhone(), s.getAboutText()));
    }
}
