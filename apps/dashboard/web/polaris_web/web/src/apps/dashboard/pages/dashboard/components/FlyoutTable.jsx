import React from 'react';
import { IndexTable, LegacyCard, Text, Badge } from '@shopify/polaris';

const headers = [
  { title: 'Endpoint', value: 'endpoint' },
  { title: 'Risk Score', value: 'riskScore' },
  { title: 'Issues', value: 'issues' },
  { title: 'Hostname', value: 'hostname' },
  { title: 'Access Type', value: 'accessType' },
  { title: 'Auth Type', value: 'authType' },
  { title: 'Sensitive Params', value: 'sensitiveParams' },
  { title: 'Last Seen', value: 'lastSeen' },
  { title: 'Discovered At', value: 'discoveredAt' },
  { title: 'Last Tested', value: 'lastTested' },
  { title: 'Source Location', value: 'sourceLocation' },
  { title: 'Collection', value: 'collection' },
  { title: 'Description', value: 'description' },
];

const dummyData = [
  {
    id: '1',
    endpoint: '/api/users',
    riskScore: 8.7,
    issues: 5,
    hostname: 'api.example.com',
    accessType: 'Public',
    authType: 'OAuth',
    sensitiveParams: 'email, password',
    lastSeen: '2024-06-01',
    discoveredAt: '2024-04-15',
    lastTested: '2024-06-02',
    sourceLocation: 'src/api/users.js',
    collection: 'User Management',
    description: 'Fetches all users',
  },
  {
    id: '2',
    endpoint: '/api/orders',
    riskScore: 6.2,
    issues: 2,
    hostname: 'orders.example.com',
    accessType: 'Private',
    authType: 'API Key',
    sensitiveParams: 'orderId, userId',
    lastSeen: '2024-05-30',
    discoveredAt: '2024-05-01',
    lastTested: '2024-06-01',
    sourceLocation: 'src/api/orders.js',
    collection: 'Order Processing',
    description: 'Handles order processing',
  },
  {
    id: '3',
    endpoint: '/api/payments',
    riskScore: 9.1,
    issues: 7,
    hostname: 'payments.example.com',
    accessType: 'Internal',
    authType: 'None',
    sensitiveParams: 'cardNumber, cvv',
    lastSeen: '2024-05-28',
    discoveredAt: '2024-03-20',
    lastTested: '2024-05-29',
    sourceLocation: 'src/api/payments.js',
    collection: 'Payments',
    description: 'Processes payment transactions',
  },
];

function FlyoutTable() {
  return (
    <div style={{ maxHeight: 300, overflowY: 'auto' }}>
      <LegacyCard>
        <LegacyCard.Section flush>
          <IndexTable
            resourceName={{ singular: 'endpoint', plural: 'endpoints' }}
            itemCount={dummyData.length}
            headings={headers.map(h => ({ title: h.title }))}
            selectable={false}
          >
            {dummyData.map((row, index) => (
              <IndexTable.Row id={row.id} key={row.id} position={index}>
                <IndexTable.Cell><Text variant="bodyMd">{row.endpoint}</Text></IndexTable.Cell>
                <IndexTable.Cell><Badge status="critical">{row.riskScore}</Badge></IndexTable.Cell>
                <IndexTable.Cell><Text variant="bodyMd">{row.issues}</Text></IndexTable.Cell>
                <IndexTable.Cell><Text variant="bodyMd">{row.hostname}</Text></IndexTable.Cell>
                <IndexTable.Cell><Text variant="bodyMd">{row.accessType}</Text></IndexTable.Cell>
                <IndexTable.Cell><Text variant="bodyMd">{row.authType}</Text></IndexTable.Cell>
                <IndexTable.Cell><Text variant="bodyMd">{row.sensitiveParams}</Text></IndexTable.Cell>
                <IndexTable.Cell><Text variant="bodyMd">{row.lastSeen}</Text></IndexTable.Cell>
                <IndexTable.Cell><Text variant="bodyMd">{row.discoveredAt}</Text></IndexTable.Cell>
                <IndexTable.Cell><Text variant="bodyMd">{row.lastTested}</Text></IndexTable.Cell>
                <IndexTable.Cell><Text variant="bodyMd">{row.sourceLocation}</Text></IndexTable.Cell>
                <IndexTable.Cell><Text variant="bodyMd">{row.collection}</Text></IndexTable.Cell>
                <IndexTable.Cell><Text variant="bodyMd">{row.description}</Text></IndexTable.Cell>
              </IndexTable.Row>
            ))}
          </IndexTable>
        </LegacyCard.Section>
      </LegacyCard>
    </div>
  );
}

export default FlyoutTable; 