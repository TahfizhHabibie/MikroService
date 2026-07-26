package habibi.pelanggan.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import habibi.pelanggan.model.Pelanggan;
import habibi.pelanggan.Repository.PelangganRepository;

@Service
public class PelangganService {

    @Autowired
    private PelangganRepository pelangganRepository;

    public List<Pelanggan> getAllPelanggan(){
        return pelangganRepository.findAll();
    }

    public Pelanggan getPelangganById(Long id){
        return pelangganRepository.findById(id).orElse(null);
    }

    public Pelanggan createPelanggan(Pelanggan pelanggan){
        return pelangganRepository.save(pelanggan);
    }

    public void deletePelanggan(Long id){
        pelangganRepository.deleteById(id);
    }
}