package Demo;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.edit.EditRequest;
import com.theokanning.openai.image.CreateImageEditRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.CreateImageVariationRequest;
import com.theokanning.openai.image.Image;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * @ClassName VIew
 * @Description
 * @Author 15014
 * @Time 2023/2/9 15:54
 * @Version 1.0
 */
public class TVIew {
    private static final String IMAGE_VARIATION = "相似图片";
    private static final String IAMGE_EDIT = "图片编辑";
    private static final String TEXT_EDIT = "文本编辑";
    private static String token = System.getenv("OPENAI_TOKEN");
    private static OpenAiService service = new OpenAiService(token, Duration.ofMinutes(10));
    private static String TEXT_GENERATION = "文本回复";
    private static String PICTURE_GENERATION = "图片生成";
    private File uploadImageFile;
    private File upLoadImageMask;

    private JPanel panel1;
    Component self = this.$$$getRootComponent$$$();
    private JButton send;
    private JButton selectFile;
    private JTextArea answer;
    private JTextArea question;
    private JButton setting;
    private JComboBox comboBox1;
    private JPanel outputPanel;
    private JPanel inputPanel;

    public TVIew() {
        $$$setupUI$$$();
        send.addActionListener(e -> {
            send.setEnabled(false);
            String text = question.getText();
            StringBuilder stringBuilder = new StringBuilder();
            try {
                String selectModel = (String) comboBox1.getSelectedItem();
                if (selectModel.equals(TEXT_GENERATION)) {
                    if (text == null || text.isEmpty()) {
                        throw new Exception("输入不能为空");
                    }
                    CompletionRequest completionRequest = CompletionRequest.builder()
                            .model(Utils.properties.getProperty("MODEL"))
                            .prompt(text)
                            .topP(Double.valueOf(Utils.properties.getProperty("TOP_P")))
                            .bestOf(Integer.valueOf(Utils.properties.getProperty("BEST_OF")))
                            .temperature(Double.valueOf(Utils.properties.getProperty("TEMPERATURE")))
                            .maxTokens(Integer.valueOf(Utils.properties.getProperty("MAX_TOKENS")))
                            .user(Utils.properties.getProperty("USER"))
                            .n(Integer.valueOf(Utils.properties.getProperty("N")))
                            .build();
                    service.createCompletion(completionRequest).getChoices().forEach(t ->
                            stringBuilder
                                    .append("回答如下：\n")
                                    .append(t.getText())
                                    .append("\n")
                    );
                    Utils.log(String.format("文本回复：问题:【%s】\n回答:\n【%s】\n", text, stringBuilder));
                } else if (selectModel.equals(TEXT_EDIT)) {
                    String instruction = answer.getText();
                    if (instruction == null || instruction.isEmpty()) {
                        throw new Exception("指令输入不能为空");
                    }
                    EditRequest editRequest = EditRequest.builder()
                            .model(Utils.properties.getProperty("MODEL"))
                            .input(text)
                            .instruction(instruction)
                            .topP(Double.valueOf(Utils.properties.getProperty("TOP_P")))
                            .temperature(Double.valueOf(Utils.properties.getProperty("TEMPERATURE")))
                            .n(Integer.valueOf(Utils.properties.getProperty("N")))
                            .build();
                    service.createEdit(editRequest).getChoices().forEach(t -> stringBuilder
                            .append("回答如下：\n")
                            .append(t.getText())
                            .append("\n")
                    );
                    Utils.log(String.format("文本编辑：问题:【%s】\n指令:【%s】，\n回答:\n【%s】\n", text, instruction, stringBuilder));
                } else if (selectModel.equals(PICTURE_GENERATION)) {
                    if (text == null || text.isEmpty()) {
                        throw new Exception("输入不能为空");
                    }
                    CreateImageRequest createImageRequest = CreateImageRequest.builder()
                            .prompt(text)
                            .responseFormat("url")
                            .user(Utils.properties.getProperty("USER"))
                            .size(Utils.properties.getProperty("SIZE"))
                            .n(Integer.valueOf(Utils.properties.getProperty("N")))
                            .build();

                    List<Image> images = service.createImage(createImageRequest).getData();
                    if (images != null) {
                        showImageList(images);
                    }
                    assert images != null;
                    Utils.log(String.format("图片生成：描述【%s】\n结果:\n【%s】\n", text, images.stream().map(Image::getUrl).reduce((a, b) -> a + "\n" + b).get()));
                } else if (selectModel.equals(IMAGE_VARIATION)) {
                    CreateImageVariationRequest createImageVariationRequest = CreateImageVariationRequest.builder()
                            .responseFormat("b64_json")
                            .user(Utils.properties.getProperty("USER"))
                            .size(Utils.properties.getProperty("SIZE"))
                            .n(Integer.valueOf(Utils.properties.getProperty("N")))
                            .build();
                    List<Image> images = service.createImageVariation(createImageVariationRequest, uploadImageFile).getData();
                    if (images != null) {
                        showImageList(images);
                    }
                    assert images != null;
                    Utils.log(String.format("相似图片：来源【%s】\n结果:\n【%s】\n", uploadImageFile.getAbsolutePath(), images.stream().map(Image::getUrl).reduce((a, b) -> a + "\n" + b).get()));
                } else if (selectModel.equals(IAMGE_EDIT)) {
                    if (text == null || text.isEmpty()) {
                        throw new Exception("输入不能为空");
                    }
                    CreateImageEditRequest createImageEditRequest = CreateImageEditRequest.builder()
                            .responseFormat("b64_json")
                            .user(Utils.properties.getProperty("USER"))
                            .size(Utils.properties.getProperty("SIZE"))
                            .prompt(text)
                            .n(Integer.valueOf(Utils.properties.getProperty("N")))
                            .build();
                    List<Image> images = service.createImageEdit(createImageEditRequest, uploadImageFile, upLoadImageMask).getData();
                    if (images != null) {
                        showImageList(images);
                    }
                    assert images != null;
                    Utils.log(String.format("图片编辑：来源【%s】，\n掩码【%s】\n描述【%s】\n结果:\n【%s】\n", uploadImageFile.getAbsolutePath(), upLoadImageMask.getAbsolutePath(), text, images.stream().map(Image::getUrl).reduce((a, b) -> a + "\n" + b).get()));
                }
            } catch (Exception exception) {
                stringBuilder.append(exception);
                stringBuilder.append("\n").append("也许需要更改配置参数");
                exception.printStackTrace();
            } finally {
                answer.setText(Utils.trimDuplicateSpace(stringBuilder.toString()));
                send.setEnabled(true);
            }
        });
        selectFile.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setDialogTitle("请选择要上传的图片");
            jFileChooser.setFileFilter(new FileNameExtensionFilter("png,jpeg,jpg", "png", "jpeg", "jpg"));
            int result = jFileChooser.showDialog(self, "选择");
            if (JFileChooser.APPROVE_OPTION == result) {
                uploadImageFile = jFileChooser.getSelectedFile();
                jFileChooser.setDialogTitle("【图片编辑】请选择图片的掩码（可选）");
                jFileChooser.setFileFilter(new FileNameExtensionFilter("png", "png"));
                result = jFileChooser.showDialog(self, "选择");
                if (JFileChooser.APPROVE_OPTION == result) {
                    upLoadImageMask = jFileChooser.getSelectedFile();
                } else {
                    upLoadImageMask = null;
                }
            }
        });
        setting.addActionListener(e -> {
            Setting dialog = new Setting();
            dialog.pack();
            dialog.setLocationRelativeTo(self);
            dialog.setVisible(true);
        });
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        panel1.setEnabled(false);
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setMinimumSize(new Dimension(524, 100));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(inputPanel, gbc);
        inputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "输入", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setAutoscrolls(true);
        scrollPane1.setHorizontalScrollBarPolicy(30);
        scrollPane1.setPreferredSize(new Dimension(516, 100));
        scrollPane1.setVerticalScrollBarPolicy(20);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 1;
        gbc.ipady = 1;
        inputPanel.add(scrollPane1, gbc);
        question = new JTextArea();
        question.setAutoscrolls(false);
        question.setLineWrap(true);
        question.setMinimumSize(new Dimension(516, 100));
        question.setRows(0);
        question.setText("");
        question.setToolTipText("请输入问题");
        question.setWrapStyleWord(true);
        scrollPane1.setViewportView(question);
        outputPanel = new JPanel();
        outputPanel.setLayout(new GridBagLayout());
        outputPanel.setMinimumSize(new Dimension(524, 100));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(outputPanel, gbc);
        outputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "输出【文本编辑的指令输入】", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setPreferredSize(new Dimension(516, 100));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 1;
        gbc.ipady = 1;
        outputPanel.add(scrollPane2, gbc);
        answer = new JTextArea();
        answer.setAutoscrolls(false);
        answer.setLineWrap(true);
        answer.setMinimumSize(new Dimension(516, 100));
        answer.setWrapStyleWord(true);
        scrollPane2.setViewportView(answer);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(panel2, gbc);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        send = new JButton();
        send.setEnabled(true);
        send.setText("发送");
        panel2.add(send);
        selectFile = new JButton();
        selectFile.setText("选择文件");
        panel2.add(selectFile);
        comboBox1 = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("文本回复");
        defaultComboBoxModel1.addElement("文本编辑");
        defaultComboBoxModel1.addElement("图片生成");
        defaultComboBoxModel1.addElement("图片编辑");
        defaultComboBoxModel1.addElement("相似图片");
        comboBox1.setModel(defaultComboBoxModel1);
        panel2.add(comboBox1);
        setting = new JButton();
        setting.setText("设置");
        panel2.add(setting);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

    private void showImageList(List<Image> images) throws Exception {
        for (Image image : images) {
            URL url = new URL(image.getUrl());
            InputStream inStream = url.openConnection().getInputStream();
            byte[] data = Utils.readInputStream(inStream);
            File imageFile = File.createTempFile(UUID.randomUUID().toString(), "png");
            Utils.writeFile(imageFile, data);
            JFrame frame = new JFrame();
            String sizeStr = Utils.properties.getProperty("SIZE");
            String[] size = sizeStr.split("X");
            if (size.length != 2) {
                size = sizeStr.split("x");
            }
            frame.setSize(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
            JPanel imagePane = new JPanel() {
                @Override
                public void paint(Graphics g) {
                    try {
                        g.drawImage(ImageIO.read(imageFile), 0, 0, null);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            frame.setTitle("点击保存");
            imagePane.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JFileChooser jFileChooser = new JFileChooser();
                    jFileChooser.setFileFilter(new FileNameExtensionFilter("png,jpeg", "png,jpeg"));
                    int result = jFileChooser.showSaveDialog(frame);
                    if (JFileChooser.APPROVE_OPTION == result) {
                        File file = jFileChooser.getSelectedFile();
                        if (!file.getName().contains(".")) {
                            file = new File(file.getParent(), file.getName() + ".png");
                        }
                        imageFile.renameTo(file);
                        frame.dispose();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
            frame.setContentPane(imagePane);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
            frame.repaint();
        }
    }

    private void createUIComponents() {
    }
}
