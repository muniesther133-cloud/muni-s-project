import java.time.Duration;
import java.time.LocalTime;

public class Doctor extends Person {
    private String specialty;
    private LocalTime workStart;
    private LocalTime workEnd;
    private double consultationFee;

    public Doctor(String id, String name, String specialty, LocalTime workStart,
                  LocalTime workEnd, double consultationFee) {
        super(id, name);
        this.specialty = Text.required(specialty, "Specialty");
        checkHours(workStart, workEnd);
        this.workStart = workStart;
        this.workEnd = workEnd;
        this.consultationFee = checkFee(consultationFee);
    }

    public static void checkHours(LocalTime start, LocalTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new InvalidDataException("Finish time must be after start time.");
        }
        if (Duration.between(start, end).toMinutes() < Appointment.DURATION_MINUTES) {
            throw new InvalidDataException("Working hours must fit at least one "
                    + Appointment.DURATION_MINUTES + "-minute appointment.");
        }
    }

    public static double checkFee(double fee) {
        if (Double.isNaN(fee) || Double.isInfinite(fee) || fee < 0) {
            throw new InvalidDataException("Consultation fee must be zero or more.");
        }
        return fee;
    }

    public String getSpecialty() { return specialty; }
    public LocalTime getWorkStart() { return workStart; }
    public LocalTime getWorkEnd() { return workEnd; }
    public double getConsultationFee() { return consultationFee; }

    public void setConsultationFee(double fee) { this.consultationFee = checkFee(fee); }

    /** "Dr. Name", without doubling the prefix if the name already has it. */
    public String getDisplayName() {
        String n = getName();
        return n.toLowerCase().startsWith("dr") ? n : "Dr. " + n;
    }

    /** True when the whole slot [start, end] sits inside the doctor's working hours. */
    public boolean worksDuring(LocalTime start, LocalTime end) {
        return !start.isBefore(workStart) && !end.isAfter(workEnd);
    }

    @Override
    public String getRole() { return "Doctor"; }

    @Override
    public String toFileLine() {
        return Text.join(getId(), getName(), specialty, workStart, workEnd, consultationFee);
    }

    @Override
    public String toString() {
        return String.format("%-5s %-22s %-16s %s-%s  KES %,.2f",
                getId(), Text.clip(getDisplayName(), 22), Text.clip(specialty, 16),
                workStart, workEnd, consultationFee);
    }
}
