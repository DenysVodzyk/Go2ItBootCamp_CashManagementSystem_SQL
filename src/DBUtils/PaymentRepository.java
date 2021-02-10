package DBUtils;

import Entity.Payment;
import Service.CustomerService;
import Service.MerchantService;

import java.io.IOException;
import java.security.PrivateKey;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepository {
    private CustomerService customerService = new CustomerService();
    private MerchantService merchantService = new MerchantService();
    private MerchantRepository merchantRepository = new MerchantRepository();

    public List<Payment> getAll() {
        List<Payment> payments = new ArrayList<>();
        Connection con = null;
        PreparedStatement stm = null;
        try {
            con = DBConnection.getConnection();
            String sql = "SELECT * FROM payment";
            stm = con.prepareStatement(sql);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp dtStamp = rs.getTimestamp("dt");
                LocalDateTime dt = dtStamp.toLocalDateTime();
                int merchantId = rs.getInt("merchantId");
                int customerId = rs.getInt("customerId");
                String goods = rs.getString("goods");
                double sumPaid = rs.getDouble("sumPaid");
                double chargePaid = rs.getDouble("chargePaid");

                payments.add(new Payment(id, dt, merchantService.getById(merchantId), customerService.getById(customerId), goods, sumPaid, chargePaid));
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    stm.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return payments;
    }

    //Lesson 7, clause 4
    public void addPayment(Payment payment) {
        Connection con = null;
        PreparedStatement stm = null;
        try {
            con = DBConnection.getConnection();
            String sql = "INSERT INTO payment(dt, merchantId, customerId, goods, sumPaid, chargePaid) VALUES (?,?,?,?,?,?)";
            stm = con.prepareStatement(sql);
            java.sql.Timestamp dt = java.sql.Timestamp.valueOf(payment.getDt());
            stm.setTimestamp(1, dt);
            stm.setInt(2, payment.getMerchantId());
            stm.setInt(3, payment.getCustomerId());
            stm.setString(4, payment.getGoods());
            stm.setDouble(5, payment.getSumPaid());
            payment.setChargePaid(payment.getSumPaid() * payment.getMerchant().getCharge() / 100);
            stm.setDouble(6, payment.getChargePaid());
            merchantRepository.update(payment);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    stm.executeUpdate();
                    stm.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }
}