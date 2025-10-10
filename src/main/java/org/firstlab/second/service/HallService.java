package org.firstlab.second.service;

import org.firstlab.second.dto.HallDTO;
import org.firstlab.second.entity.Hall;
import org.firstlab.second.repository.HallRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HallService {

    private final HallRepository hallRepository;

    public HallService(HallRepository hallRepository) {
        this.hallRepository = hallRepository;
    }

    public HallDTO createHall(HallDTO hallDTO) {
        Hall hall = convertToEntity(hallDTO);
        Hall savedHall = hallRepository.save(hall);
        return convertToDTO(savedHall);
    }

    public List<HallDTO> getAllHalls() {
        return hallRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public HallDTO getHallById(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hall with ID " + id + " not found"));
        return convertToDTO(hall);
    }

    public HallDTO updateHall(Long id, HallDTO hallDTO) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hall with ID " + id + " not found"));

        hall.setName(hallDTO.getName());
        hall.setCapacity(hallDTO.getCapacity());

        Hall updatedHall = hallRepository.save(hall);
        return convertToDTO(updatedHall);
    }

    public void deleteHall(Long id) {
        if (!hallRepository.existsById(id)) {
            throw new RuntimeException("Hall with ID " + id + " not found");
        }
        hallRepository.deleteById(id);
    }

    private HallDTO convertToDTO(Hall hall) {
        return new HallDTO(
                hall.getId(),
                hall.getName(),
                hall.getCapacity()
        );
    }

    private Hall convertToEntity(HallDTO dto) {
        return new Hall(
                dto.getId(),
                dto.getName(),
                dto.getCapacity()
        );
    }
}

