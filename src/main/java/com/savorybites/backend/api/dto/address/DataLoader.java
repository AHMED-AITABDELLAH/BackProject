package com.savorybites.backend.api.dto.address;

import com.savorybites.backend.model.addressDetailModels.Region;
import com.savorybites.backend.model.addressDetailModels.Commune;
import com.savorybites.backend.model.addressDetailModels.Province;
import com.savorybites.backend.repository.addressDetailRepository.CommuneRepository;
import com.savorybites.backend.repository.addressDetailRepository.ProvinceRepository;
import com.savorybites.backend.repository.addressDetailRepository.RegionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final RegionRepository regionRepository;

    private final ProvinceRepository provinceRepository;

    private final CommuneRepository communeRepository;


    public DataLoader(RegionRepository regionRepository, ProvinceRepository provinceRepository, CommuneRepository communeRepository) {
        this.regionRepository = regionRepository;
        this.provinceRepository = provinceRepository;
        this.communeRepository = communeRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // Run only if there are no regions (i.e., no seed data yet)
        if (regionRepository.count() > 0) {
            System.out.println("Data already seeded, skipping...");
            return;
        }

        // Insert regions
        Region region1 = saveRegion("Tanger-Tetouan-Al Hoceima");
        Region region2 = saveRegion("Oriental");
        Region region3 = saveRegion("Fès-Meknès");
        Region region4 = saveRegion("Rabat-Salé-Kénitra");
        Region region5 = saveRegion("Béni Mellal-Khénifra");
        Region region6 = saveRegion("Casablanca-Settat");
        Region region7 = saveRegion("Marrakech-Safi");
        Region region8 = saveRegion("Drâa-Tafilalet");
        Region region9 = saveRegion("Souss-Massa");
        Region region10 = saveRegion("Guelmim-Oued Noun");
        Region region11 = saveRegion("Laâyoune-Sakia El Hamra");
        Region region12 = saveRegion("Dakhla-Oued Ed-Dahab");

        // Insert provinces for Souss-Massa (region9)
        Province province1 = saveProvince("Agadir-Ida Ou Tanane", region9);
        Province province2 = saveProvince("Inezgane-Aït Melloul", region9);
        Province province3 = saveProvince("Chtouka-Aït Baha", region9);
        Province province4 = saveProvince("Taroudant", region9);
        Province province5 = saveProvince("Tiznit", region9);
        Province province6 = saveProvince("Tata", region9);

        // Insert communes
        // Province 1 communes
        saveCommune("Agadir", "Urban", province1);
        saveCommune("Aourir", "Rural", province1);
        saveCommune("Amskroud", "Rural", province1);
        saveCommune("Drarga", "Rural", province1);
        saveCommune("Taghazout", "Rural", province1);
        saveCommune("Tamdroust", "Rural", province1);
        saveCommune("Tikywine", "Urban", province1);

        // Province 2 communes
        saveCommune("Inezgane", "Urban", province2);
        saveCommune("Aït Melloul", "Urban", province2);
        saveCommune("Dcheira El Jihadia", "Urban", province2);
        saveCommune("Temsia", "Rural", province2);
        saveCommune("Oulad Dahou", "Rural", province2);
        saveCommune("Lqliâa", "Urban", province2);

        // Province 3 communes
        saveCommune("Biougra", "Urban", province3);
        saveCommune("Massa", "Rural", province3);
        saveCommune("Sidi Bibi", "Rural", province3);
        saveCommune("Belfaa", "Rural", province3);
        saveCommune("Aït Baha", "Urban", province3);
        saveCommune("Aït Milk", "Rural", province3);
        saveCommune("Tassila", "Rural", province3);

        // Province 4 communes
        saveCommune("Taroudant", "Urban", province4);
        saveCommune("Oulad Teima", "Urban", province4);
        saveCommune("El Guerdane", "Urban", province4);
        saveCommune("Aoulouz", "Urban", province4);
        saveCommune("Aït Iaaza", "Urban", province4);
        saveCommune("Freija", "Rural", province4);
        saveCommune("Argana", "Rural", province4);

        // Province 5 communes
        saveCommune("Tiznit", "Urban", province5);
        saveCommune("Arbaa Rasmouka", "Rural", province5);
        saveCommune("Tighmi", "Rural", province5);
        saveCommune("Reggada", "Rural", province5);
        saveCommune("Aït Oualla", "Rural", province5);
        saveCommune("Sidi Ahmed Ou Moussa", "Rural", province5);

        // Province 6 communes
        saveCommune("Tata", "Urban", province6);
        saveCommune("Akka", "Rural", province6);
        saveCommune("Tissint", "Rural", province6);
        saveCommune("Foum Zguid", "Urban", province6);
        saveCommune("Aït Ouabelli", "Rural", province6);
        saveCommune("Tizounine", "Rural", province6);
    }

    private Region saveRegion(String name) {
        Region region = new Region();
        region.setRegionName(name);
        return regionRepository.save(region);
    }

    private Province saveProvince(String name, Region region) {
        Province province = new Province();
        province.setProvinceName(name);
        province.setRegion(region);
        return provinceRepository.save(province);
    }

    private void saveCommune(String name, String type, Province province) {
        Commune commune = new Commune();
        commune.setCommuneName(name);
        commune.setCommuneType(type);
        commune.setProvince(province);
        communeRepository.save(commune);
    }
}