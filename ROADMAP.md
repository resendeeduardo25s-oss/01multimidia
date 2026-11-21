# Plano de desenvolvimento — Minha Multimídia 2.0

## Etapa 1 — Base visual Premium Sport (incluída no pacote)
- Cinco temas de dashboard: Simbionte, Simbionte Carnificina, Garagem Âmbar, Costa Neon e NFS Underground Garage.
- Seis veículos enviados.
- Garagem e paleta de pintura.
- Tela de boas-vindas com nome dinâmico do carro.
- Fundo personalizado com URI persistida.
- Preferências para exibir/ocultar widgets.
- Velocidade via GPS.

## Etapa 2 — Veículo e aparência
- Máscaras de lataria por veículo para recoloração seletiva.
- Ajuste de enquadramento por proporção da multimídia.
- Animação de entrada, iluminação e rodas.
- Temas instaláveis/exportáveis em pacote próprio.
- Persistência de perfil por veículo (carro, cor, tema e nome).

## Etapa 3 — Integração OBD2
- Tela de busca e conexão Bluetooth clássico e BLE.
- Perfil de adaptador e estado de conexão.
- Leitura real de tensão da bateria, RPM e temperatura do líquido de arrefecimento quando a ECU fornecer os PIDs.
- Consumo apenas quando houver PID/dados compatíveis.
- Fallback GPS para velocidade, indicando a origem do dado.
- Alertas configuráveis de temperatura e tensão.
- Nunca exibir valores fictícios como telemetria real.

## Etapa 4 — Cockpit e mídia
- Medidores configuráveis e reordenáveis.
- Player com leitura de mídia/notificações conforme permissões.
- Atalhos configuráveis para apps instalados.
- Modo noturno e brilho automático quando suportado pela central.
- Inicialização automática após boot, com opção de desativar.

## Etapa 5 — Testes na central
- Identificar resolução e densidade da tela.
- Testar rotação, modo imersivo, boot e suspensão.
- Verificar GPS, Bluetooth, áudio e permissões.
- Testar com o adaptador OBD2 real.
- Ajustar consumo de memória e fluidez.

## Critérios de segurança e confiabilidade
- Permissões solicitadas apenas quando a função correspondente for ativada.
- Indicador claro de GPS versus OBD2.
- Sem comandos para controlar sistemas físicos do veículo nesta versão.
- Dados OBD2 tratados como leitura; sem escrita de comandos na ECU.
