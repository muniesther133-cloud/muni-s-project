import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AppointmentManager {
    private final List<Appointment> appointments = new ArrayList<>();
    private int counter = 0;

    /**
     * Books a slot after checking: not in the past, inside the doctor's working hours,
     * and no clash for the doctor (or the patient) with an active appointment.
     */
    public Appointment book(Patient patient, Doctor doctor, LocalDate date, LocalTime start)
            throws DoubleBookingException {
        LocalTime end = start.plusMinutes(Appointment.DURATION_MINUTES);
        if (!end.isAfter(start)) {
            throw new InvalidDataException("An appointment cannot run past midnight.");
        }
        if (LocalDateTime.of(date, start).isBefore(LocalDateTime.now())) {
            throw new InvalidDataException("That time has already passed.");
        }
        if (!doctor.worksDuring(start, end)) {
            throw new InvalidDataException(doctor.getDisplayName() + " only works "
                    + doctor.getWorkStart() + "-" + doctor.getWorkEnd() + ".");
        }

        Appointment candidate = new Appointment(Text.makeId('A', counter + 1), patient, doctor,
                date, start, doctor.getConsultationFee(), Appointment.Status.BOOKED);

        for (Appointment existing : appointments) {
            if (!existing.isActive() || !existing.overlapsWith(candidate)) continue;
            if (existing.getDoctor().getId().equals(doctor.getId())) {
                throw new DoubleBookingException(doctor.getDisplayName() + " is already booked "
                        + existing.getStartTime() + "-" + existing.getEndTime() + " on " + date + ".");
            }
            if (existing.getPatient().getId().equals(patient.getId())) {
                throw new DoubleBookingException(patient.getName() + " already has an appointment at "
                        + existing.getStartTime() + " on " + date + " (" + existing.getId() + ").");
            }
        }

        counter++;
        appointments.add(candidate);
        return candidate;
    }

    public void addLoaded(Appointment a) {
        appointments.add(a);
        counter = Math.max(counter, Text.idNumber(a.getId()));
    }

    public Appointment get(String id) {
        for (Appointment a : appointments) {
            if (a.getId().equalsIgnoreCase(id.trim())) return a;
        }
        throw new NotFoundException("No appointment with ID " + id.trim() + ".");
    }

    public Appointment cancel(String id) {
        Appointment a = get(id);
        a.cancel();
        return a;
    }

    /** Free 30-minute slots for a doctor on a date (past times are left out). */
    public List<LocalTime> freeSlots(Doctor doctor, LocalDate date) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime t = doctor.getWorkStart();
        while (true) {
            final LocalTime slotStart = t;
            final LocalTime slotEnd = t.plusMinutes(Appointment.DURATION_MINUTES);
            if (!slotEnd.isAfter(slotStart) || slotEnd.isAfter(doctor.getWorkEnd())) break;

            boolean taken = false;
            for (Appointment a : appointments) {
                if (a.isActive() && a.getDoctor().getId().equals(doctor.getId())
                        && a.getDate().equals(date)
                        && a.getStartTime().isBefore(slotEnd) && slotStart.isBefore(a.getEndTime())) {
                    taken = true;
                    break;
                }
            }
            boolean past = LocalDateTime.of(date, slotStart).isBefore(LocalDateTime.now());
            if (!taken && !past) slots.add(slotStart);
            t = slotEnd;
        }
        return slots;
    }

    public List<Appointment> all() { return sorted(appointments); }

    public List<Appointment> forDoctor(String doctorId) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment a : appointments) {
            if (a.getDoctor().getId().equals(doctorId)) list.add(a);
        }
        return sorted(list);
    }

    public List<Appointment> forPatient(String patientId) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment a : appointments) {
            if (a.getPatient().getId().equals(patientId)) list.add(a);
        }
        return sorted(list);
    }

    public int count() { return appointments.size(); }

    private List<Appointment> sorted(List<Appointment> list) {
        List<Appointment> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparing(Appointment::getDate).thenComparing(Appointment::getStartTime));
        return copy;
    }
}
