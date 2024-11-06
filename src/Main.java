import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {
    /* GUI 컴포넌트*/
    // panel1: 전체적인 레이아웃
    private JPanel panel1;
    // EmployeeTable: 직원 정보를 보여줄 테이블
    private JTable EmployeeTable;
    // 보여질 column을 선택할 수 있는 checkboxes
    private JCheckBox   nameCheckBox, ssnCheckBox, bdateCheckBox, addressCheckBox,
                        sexCheckBox, salaryCheckBox, supervisorCheckBox, departmentCheckBox;

    // 검색 범위를 선택할 수 있는 ComboBoxes
    private JComboBox<String> SearchRangeComboBox, genderComboBox, departmentComboBox, cityComboBox;

    // salaryTextField: 연봉을 입력할 수 있는 텍스트 필드. 입력한 연봉 이상의 직원을 검색을 위함
    private JTextField salaryTextField;
    
    // updateButton: table 수정을 위한 버튼
    // searchButton: 정해진 조건에 따라 검색을 하기 위한 버튼
    private JButton updateButton, searchButton;

    // DB 연결 정보
    private static final String DB_URL = "jdbc:mysql://localhost:3306/COMPANY";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "BradleyRyu";

    public Main() {
        setTitle("Simple GUI App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // 창의 layout 설정 (1200x800)
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // components 초기화
        initComponents();
        layoutComponents();

        // Establish database connection and display table
        try {
            Connection connection = getConnection();
            if (connection != null) {
                System.out.println("DB에 성공적으로 연결했습니다!");
                displayEmployeeTable(connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setVisible(true);
    }

    // GUI 컴포넌트 초기화 함수
    private void initComponents() {
        panel1 = new JPanel();

        // 검색 범위로 사용할 ComboBox. 성별, 부서, 연봉, 거주 도시를 선택
        SearchRangeComboBox = new JComboBox<>(new String[]{"전체", "성별", "부서", "거주 도시", "연봉"});

        // 검색 범위 성별 선택 시 보여줄 선택지
        genderComboBox = new JComboBox<>(new String[]{"M", "F"});

        // 검색 범위 부서 선택 시 보여줄 선택지
        departmentComboBox = new JComboBox<>();

        // 검색 범위 거주 도시 선택 시 보여줄 선택지
        cityComboBox = new JComboBox<>();

        // 검색 범위 연봉 선택 시, 입력한 연봉 이상의 직원을 검색하기 위한 텍스트 필드
        salaryTextField = new JTextField(20);

        // table 초기화
        EmployeeTable = new JTable();

        // table의 각 column에 해당하는 checkbox들. 기본적으로 모두 선택되어 있고 해제 시 해당 column이 보이지 않음
        nameCheckBox = new JCheckBox("Name", true);
        ssnCheckBox = new JCheckBox("SSN", true);
        bdateCheckBox = new JCheckBox("Birth Date", true);
        addressCheckBox = new JCheckBox("Address", true);
        sexCheckBox = new JCheckBox("Sex", true);
        salaryCheckBox = new JCheckBox("Salary", true);
        supervisorCheckBox = new JCheckBox("Supervisor", true);
        departmentCheckBox = new JCheckBox("Department", true);

        // updateButton: table을 업데이트하기 위한 버튼
        updateButton = new JButton("Update Table");

        // 주어진 조건으로 검색을 실행할 버튼
        searchButton = new JButton("Search");

        // checkbox에 item listener 추가.
        // checkbox를 해제하거나 선택하는 event 발생 시, 해당 column을 보이거나 숨기기 위함
        ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                try {
                    Connection connection = getConnection();
                    if (connection != null) {
                        displayEmployeeTable(connection);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        };

        // 각 checkboxes에 item listener 추가
        nameCheckBox.addItemListener(itemListener);
        ssnCheckBox.addItemListener(itemListener);
        bdateCheckBox.addItemListener(itemListener);
        addressCheckBox.addItemListener(itemListener);
        sexCheckBox.addItemListener(itemListener);
        salaryCheckBox.addItemListener(itemListener);
        supervisorCheckBox.addItemListener(itemListener);
        departmentCheckBox.addItemListener(itemListener);

        // 검색 범위 설정 combobox에 item listener 추가
        SearchRangeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // 각 검색 범위 설정에 따라서, 세부 범위 설정을 위한 combobox 혹은 textfield를 보이게 하거나 숨김
                // SELECT 될 시
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // selected: 선택된 검색 범위
                    String selected = (String) e.getItem();

                    // item listen 시 기본적으로 모두 invisible하게 설정
                    genderComboBox.setVisible(false);
                    departmentComboBox.setVisible(false);
                    salaryTextField.setVisible(false);
                    cityComboBox.setVisible(false);

                    // 검색 범위에서 "성별" 선택 시
                    if ("성별".equals(selected)) {
                        // 성별 선택을 위한 combobox를 보이게 함 (M, F) 선택
                        genderComboBox.setVisible(true);
                    // 검색 범위에서 "부서" 선택 시
                    } else if ("부서".equals(selected)) {
                        // 부서 선택을 위한 combobox를 보이게 함
                        departmentComboBox.setVisible(true);
                        displayDepartmentComboBox();
                    // 검색 범위에서 "연봉" 선택 시
                    } else if ("연봉".equals(selected)) {
                        // 연봉 입력을 위한 textfield를 보이게 함
                        salaryTextField.setVisible(true);
                    // 검색 범위에서 "거주 도시" 선택 시
                    } else if ("거주 도시".equals(selected)) {
                        // 거주 도시 선택을 위한 combobox를 보이게 함. state 이름과 city name으로 구성.
                        cityComboBox.setVisible(true);
                        displayCityComboBox();
                    }
                    panel1.revalidate();
                    panel1.repaint();
                }
            }
        });
    }

    private void layoutComponents() {
        panel1.setLayout(new BorderLayout());
        // checkboxPanel: column 선택을 위한 panel. 8개의 column checkbox를 가로로 나열
        JPanel checkBoxPanel = new JPanel(new GridLayout(1, 8));

        // searchPanel: 검색 범위 설정을 위한 panel. combobox와 각 검색 범위에 따른 세부 범위 설정을 위한 combobox, textfield, button을 가로로 나열
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("검색 범위:"));

        // 각 components를 추가
        searchPanel.add(SearchRangeComboBox);
        searchPanel.add(genderComboBox);
        searchPanel.add(departmentComboBox);
        searchPanel.add(salaryTextField);
        searchPanel.add(cityComboBox);
        searchPanel.add(searchButton);

        // 기본적으로 모두 invisible하게 설정
        genderComboBox.setVisible(false);
        departmentComboBox.setVisible(false);
        salaryTextField.setVisible(false);
        cityComboBox.setVisible(false);

        // northPanel: searchPanel과 checkBoxPanel을 세로로 나열
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(searchPanel, BorderLayout.NORTH);
        northPanel.add(checkBoxPanel, BorderLayout.SOUTH);

        // searchPanel과 checkBoxPanel을 함께 추가
        panel1.add(northPanel, BorderLayout.NORTH);
        // EmployeeTable을 그 아래에 추가
        panel1.add(new JScrollPane(EmployeeTable), BorderLayout.CENTER);
        // 아래쪽에 update button 추가
        panel1.add(updateButton, BorderLayout.SOUTH);

        // checkBoxPanel에 각 column checkbox 추가
        checkBoxPanel.add(nameCheckBox);
        checkBoxPanel.add(ssnCheckBox);
        checkBoxPanel.add(bdateCheckBox);
        checkBoxPanel.add(addressCheckBox);
        checkBoxPanel.add(sexCheckBox);
        checkBoxPanel.add(salaryCheckBox);
        checkBoxPanel.add(supervisorCheckBox);
        checkBoxPanel.add(departmentCheckBox);

        // frame에 panel1 추가
        add(panel1);

        // update button 클릭 시 table 업데이트
        updateButton.addActionListener(e -> {
            try {
                Connection connection = getConnection();
                if (connection != null) {
                    displayEmployeeTable(connection);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // 검색 버튼 클릭 시 주어진 조건에 따라 검색하여 보이는 table update
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Connection connection = getConnection();
                    if (connection != null) {
                        String selectedRange = (String) SearchRangeComboBox.getSelectedItem();
                        String query = buildQuery(selectedRange);
                        displayEmployeeTableWithQuery(connection, query);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // DB에 연결을 하기 위한 함수
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // 전체 직원 정보를 보여주는 table을 보여주기 위한 함수
    private void displayEmployeeTable(Connection connection) {
        String query = buildQuery("전체");
        displayEmployeeTableWithQuery(connection, query);
    }

    // 주어진 query에 따라 table을 보여주기 위한 함수
    private void displayEmployeeTableWithQuery(Connection connection, String query) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // table에서 column 이름들을 가져옴
            ResultSetMetaData metaData = rs.getMetaData();
            // column의 개수를 가져옴
            int columnCount = metaData.getColumnCount();
            // column 이름을 columnNames에 저장
            String[] columnNames = new String[columnCount];
            // 각 column의 이름을 columnNames에 저장
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = metaData.getColumnName(i);
            }

            // DefaultTableModel 객체 생성
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
            // table에 각 tuples 추가
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

            // table에 model을 설정
            EmployeeTable.setModel(model);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DEPARTMENT table에서 부서 이름을 가져와서 departmentComboBox에 추가
    private void displayDepartmentComboBox() {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT Dname FROM DEPARTMENT")) {
            // departmentComboBox 초기화하고 departmentComboBox에 각 department 추가
            departmentComboBox.removeAllItems();
            while (rs.next()) {
                departmentComboBox.addItem(rs.getString("Dname").trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // EMPLOYEE table에서 거주 도시를 가져와서 cityComboBox에 추가
    private void displayCityComboBox() {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT SUBSTRING_INDEX(Address, ',', -2) AS CityState FROM EMPLOYEE")) {
            // cityComboBox 초기화하고 cityComboBox에 각 city 추가
            cityComboBox.removeAllItems();
            while (rs.next()) {
                cityComboBox.addItem(rs.getString("CityState").trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 기본 query를 생성하는 함수
    private String buildQuery(String selectedRange) {
        List<String> selectedColumns = new ArrayList<>();
        if (nameCheckBox.isSelected()) selectedColumns.add("E.Fname");
        if (ssnCheckBox.isSelected()) selectedColumns.add("E.Ssn");
        if (bdateCheckBox.isSelected()) selectedColumns.add("E.Bdate");
        if (addressCheckBox.isSelected()) selectedColumns.add("E.Address");
        if (sexCheckBox.isSelected()) selectedColumns.add("E.Sex");
        if (salaryCheckBox.isSelected()) selectedColumns.add("E.Salary");
        if (supervisorCheckBox.isSelected()) selectedColumns.add("E.Super_ssn");
        if (departmentCheckBox.isSelected()) selectedColumns.add("D.Dname");

        // 아무것도 선택하지 않았을 시 전체 column을 보임
        if (selectedColumns.isEmpty()) {
            selectedColumns.add("*");
        }

        // 각 선택된 columns에 대해 SELECT query 생성
        String columns = String.join(", ", selectedColumns);

        // 기본적으로 display할 table은 EMPLOYEE table과 DEPARTMENT table을 JOIN한 table
        String query = "SELECT " + columns + " FROM EMPLOYEE E JOIN DEPARTMENT D ON E.Dno = D.Dnumber";

        // 검색 범위(성별, 부서, 연봉, 거주 도시 등)에 따른 세부 조건 display를 위한 query 추가
        if ("성별".equals(selectedRange)) {
            query += " WHERE E.Sex = '" + genderComboBox.getSelectedItem() + "'";
        } else if ("부서".equals(selectedRange)) {
            query += " WHERE D.Dname = '" + departmentComboBox.getSelectedItem() + "'";
        } else if ("연봉".equals(selectedRange)) {
            query += " WHERE E.Salary > " + salaryTextField.getText();
        } else if ("거주 도시".equals(selectedRange)) {
            String selectedCity = (String) cityComboBox.getSelectedItem();
            if (selectedCity != null) {
                // cityComboBox에서 선택된 도시 이름과 주 이름으로 나누어 각각의 조건으로 추가
                String[] parts = selectedCity.split(", ");
                query += " WHERE E.Address LIKE '%" + parts[0] + "%' AND E.Address LIKE '%" + parts[1] + "%'";
            }
        }

        // 최종 query 반환
        return query;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}