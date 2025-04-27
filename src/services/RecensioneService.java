package services;

import com.example.cache.JsonRepository;
import com.example.models.Recensione;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RecensioneService {
    private final JsonRepository<Recensione> recensioneRepository;

    public RecensioneService(JsonRepository<Recensione> recensioneRepository) {
        this.recensioneRepository = recensioneRepository;
    }

    public boolean isPositiva(int rate){
        if(rate >= 3)
            return true;
        return false;
    }

    public String votoParola(int rate){
        switch(rate){
            case 0:
                return "Insufficiente";
            case 1:
                return "Scarso";
            case 2:
                return "Mediocre";
            case 3:
                return "Soddisfacente";
            case 4:
                return "Ottimo";
            case 5:
                return "Eccellente";
        }
        return "";
    }

    public String quantoTempoDaRecensione(LocalDateTime date){
        LocalDateTime current_date = LocalDateTime.now();

        if (ChronoUnit.YEARS.between(date, current_date) > 0){
            long yearsgap = ChronoUnit.YEARS.between(date, current_date);
            if (yearsgap == 1)
                return "1 anno fa";
            else
                return yearsgap + " anni fa";
        }

        if (ChronoUnit.MONTHS.between(date, current_date) > 0 && ChronoUnit.MONTHS.between(date, current_date) < 12){
            long monthsgap = ChronoUnit.MONTHS.between(date, current_date);
            if (monthsgap == 1)
                return "1 mese fa";
            else
                return monthsgap + " mesi fa";
        }

        if(ChronoUnit.HOURS.between(date, current_date) > 0 && ChronoUnit.HOURS.between(date, current_date) < 24){
            long hoursgap = ChronoUnit.HOURS.between(date, current_date);
            if (hoursgap == 1)
                return "1 ora fa";
            else
                return hoursgap + " ore fa";
        }

        if (ChronoUnit.MINUTES.between(date, current_date) > 0 && ChronoUnit.MINUTES.between(date, current_date) < 60) {
            long minutesgap = ChronoUnit.MINUTES.between(date, current_date);
            if (minutesgap == 1)
                return "1 minuto fa";
            else
                return minutesgap + " minuti fa";
        }

        if(ChronoUnit.SECONDS.between(date, current_date) > 0 && ChronoUnit.SECONDS.between(date, current_date) < 60) {
            long secondsgap = ChronoUnit.SECONDS.between(date, current_date);
            if (secondsgap == 1)
                return "1 secondo fa";
            else
                return secondsgap + " secondi fa";
        }

        return "";
    }
}
