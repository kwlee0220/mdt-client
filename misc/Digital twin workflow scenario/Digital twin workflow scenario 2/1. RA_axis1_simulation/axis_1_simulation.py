import torch
import torch.nn as nn
import numpy as np
import argparse
import os

# 1. FlexibleLSTM Class
class FlexibleLSTM(nn.Module):
    def __init__(self, input_size, hidden_size, num_layers, sequence_length, output_size):
        super(FlexibleLSTM, self).__init__()
        self.lstm = nn.LSTM(input_size=input_size, hidden_size=hidden_size, num_layers=num_layers, batch_first=True)
        self.fc = nn.Linear(hidden_size * sequence_length, output_size)  
        self.drop_out = nn.Dropout()

    def forward(self, x):
        lstm_out, _ = self.lstm(x)
        lstm_out = lstm_out.contiguous().view(x.size(0), -1)  
        out = self.drop_out(lstm_out)
        out = self.fc(out)
        
        return out

# 2. text data load
def load_data_from_text(file_path, sequence_length, input_size):
    data = []
    with open(file_path, 'r') as f:
        for line in f:
            values = list(map(float, line.strip().split(',')))
            data.append(values)
    data = np.array(data).astype(np.float32)
    expected_size = sequence_length * input_size
    if data.size % expected_size != 0:
        raise ValueError(f"Data size {data.size} is not a multiple of sequence_length * input_size ({sequence_length} * {input_size} = {expected_size})")

    return data.reshape(-1, sequence_length, input_size)

# 3. prediction function
def predict_new_data(model, data):
    model.eval()
    with torch.no_grad():
        data_tensor = torch.tensor(data).unsqueeze(0)  
        outputs = model(data_tensor)
        _, predicted = torch.max(outputs, 1)
    return predicted.item()

if __name__ == '__main__':
    # 4. Hyperparameter for LSTM model
    input_size = 1       # 입력 데이터의 크기
    hidden_size = 128     # LSTM의 은닉층 크기
    num_layers = 1        # LSTM 레이어 개수
    sequence_length = 5  # 시퀀스 길이
    output_size = 1       # 출력 클래스 수
    
    # Model initialization    
    model_path = os.path.join(os.getcwd(), "axis_1_model.ckpt")
    model = FlexibleLSTM(input_size, hidden_size, num_layers, sequence_length, output_size)
    model.load_state_dict(torch.load(model_path))

    #Arguement parshing
    parser = argparse.ArgumentParser(description="RNN-based Fault detection (Long-term)")
    parser.add_argument("input_path", type=str, help="path to the input serial data")
    args = parser.parse_args()
    file_path =args.input_path

    # Model execution
    text_data = load_data_from_text(file_path, sequence_length, input_size)
    predicted_label = predict_new_data(model, text_data[0])

    # Print result
    print(predicted_label)

