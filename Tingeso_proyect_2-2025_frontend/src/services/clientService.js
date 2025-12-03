import httpClient from './http-common';

const create = data => {
  return httpClient.post('/api/clients', data);
}

const getAll = () => {
  return httpClient.get('/api/clients/');
}

const getByRun = run => {
  return httpClient.get(`/api/clients/${run}`);
}

const getByStatus = status => {
  return httpClient.get(`/api/clients/status/${status}`);
}

const update = (run, data) => {
  return httpClient.put(`/api/clients/client/${run}`, data);
}

export default { create, getAll, getByRun, getByStatus, update };