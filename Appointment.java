import java.time.LocalDate;
import java.time.LocalTime;

/** A booked slot between one patient and one doctor. It is schedulable and billable. */
public class Appointment implements Schedulable, Billable {
    /** Freedom point: every appointment lasts 30 minutes. */
    public static final int DURATION_MINUTES = 30;

    public enum Status { BOOKED, COMPLETED, CANCELLED }

    private final String id;
    private final Patient patient;
    private final Doctor doctor;
    private final LocalDate date;
    private final LocalTime startTime;
    private final double fee;      // fee is frozen at booking time
    private Status status;

    public Appointment(String id, Patient patient, Doctor doctor, LocalDate date,
                       LocalTime startTime, double fee, Status status) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.date = date;
        this.startTime = startTime;
        this.fee = fee;
        this.status = status;
    }

    public String getId() { return id; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public Status getStatus() { return status; }

    @Override
    public LocalDate getDate() { return date; }

    @Override
    public LocalTime getStartTime() { return startTime; }

    @Override
    public LocalTime getEndTime() { return startTime.plusMinutes(DURATION_MINUTES); }

    @Override
    public double getFee() { return fee; }

    @Override
    public String getBillDescription() {
        return "Consultation with " + doctor.getDisplayName() + " (" + date + ")";
    }

    public boolean isActive() { return status != Status.CANCELLED; }
    public boolean isBooked() { return status == Status.BOOKED; }

    public void markCompleted() {
        if (!isBooked()) {
            throw new InvalidDataException("Appointment " + id + " is " + status + ", not booked.");
        }
        status = Status.COMPLETED;
    }

    public void cancel() {
        if (!isBooked()) {
            throw new InvalidDataException("Only booked appointments can be cancelled ("
                    + id + " is " + status + ").");
        }
        status = Status.CANCELLED;
    }

    public String toFileLine() {
        return Text.join(id, patient.getId(), doctor.getId(), date, startTime, fee, status);
    }

    @Override
    public String toString() {
        return String.format("%-5s %s %s-%s  %-18s with %-20s %s",
                id, date, startTime, getEndTime(), Text.clip(patient.getName(), 18),
                Text.clip(doctor.getDisplayName(), 20), status);
    }
}
